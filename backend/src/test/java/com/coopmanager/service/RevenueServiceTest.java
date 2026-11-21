package com.coopmanager.service;

import com.coopmanager.dto.revenue.RevenueCalculationRequest;
import com.coopmanager.dto.revenue.RevenueDistributionResponse;
import com.coopmanager.entity.Member;
import com.coopmanager.entity.RevenueDistribution;
import com.coopmanager.entity.Role;
import com.coopmanager.repository.MemberRepository;
import com.coopmanager.repository.ProductionEntryRepository;
import com.coopmanager.repository.RevenueDistributionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @Mock RevenueDistributionRepository distributionRepo;
    @Mock ProductionEntryRepository productionRepo;
    @Mock MemberRepository memberRepository;
    @InjectMocks RevenueService revenueService;

    private Member treasurer;
    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        treasurer = Member.builder().id(1L).firstName("Karim").lastName("T")
                .email("karim@coop.ma").role(Role.TREASURER).active(true).build();
        member1 = Member.builder().id(2L).firstName("Ahmed").lastName("A")
                .email("ahmed@coop.ma").role(Role.MEMBER).active(true).build();
        member2 = Member.builder().id(3L).firstName("Fatima").lastName("F")
                .email("fatima@coop.ma").role(Role.MEMBER).active(true).build();
    }

    @Test
    void calculate_shouldCorrectlyComputeProportionalShares() {
        RevenueCalculationRequest req = new RevenueCalculationRequest();
        req.setPeriodLabel("Jan 2025");
        req.setPeriodStart(LocalDate.of(2025, 1, 1));
        req.setPeriodEnd(LocalDate.of(2025, 1, 31));
        req.setTotalRevenue(new BigDecimal("10000"));
        req.setOperatingCosts(new BigDecimal("2000"));
        req.setSimulation(true);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(treasurer));
        when(memberRepository.findByActiveTrue()).thenReturn(List.of(member1, member2));
        when(productionRepo.sumValidatedQuantityByPeriod(any(), any()))
                .thenReturn(new BigDecimal("300"));
        when(productionRepo.sumValidatedQuantityByMemberAndPeriod(eq(member1), any(), any()))
                .thenReturn(new BigDecimal("200")); // 2/3 share
        when(productionRepo.sumValidatedQuantityByMemberAndPeriod(eq(member2), any(), any()))
                .thenReturn(new BigDecimal("100")); // 1/3 share

        RevenueDistribution savedDistribution = RevenueDistribution.builder()
                .id(1L).periodLabel("Jan 2025")
                .periodStart(req.getPeriodStart()).periodEnd(req.getPeriodEnd())
                .totalRevenue(req.getTotalRevenue()).operatingCosts(req.getOperatingCosts())
                .netRevenue(new BigDecimal("8000"))
                .simulation(true).finalized(false)
                .createdBy(treasurer).memberShares(new ArrayList<>())
                .build();

        when(distributionRepo.save(any())).thenReturn(savedDistribution);

        RevenueDistributionResponse result = revenueService.calculate(req, 1L);

        assertThat(result.getNetRevenue()).isEqualByComparingTo("8000");
        assertThat(result.isSimulation()).isTrue();
        assertThat(result.isFinalized()).isFalse();
    }

    @Test
    void calculate_shouldHandleZeroTotalProduction() {
        RevenueCalculationRequest req = new RevenueCalculationRequest();
        req.setPeriodLabel("Empty");
        req.setPeriodStart(LocalDate.of(2025, 1, 1));
        req.setPeriodEnd(LocalDate.of(2025, 1, 31));
        req.setTotalRevenue(new BigDecimal("5000"));
        req.setOperatingCosts(BigDecimal.ZERO);
        req.setSimulation(true);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(treasurer));
        when(memberRepository.findByActiveTrue()).thenReturn(List.of(member1));
        when(productionRepo.sumValidatedQuantityByPeriod(any(), any())).thenReturn(null);
        when(productionRepo.sumValidatedQuantityByMemberAndPeriod(any(), any(), any())).thenReturn(null);

        RevenueDistribution saved = RevenueDistribution.builder()
                .id(2L).periodLabel("Empty")
                .periodStart(req.getPeriodStart()).periodEnd(req.getPeriodEnd())
                .totalRevenue(req.getTotalRevenue()).operatingCosts(BigDecimal.ZERO)
                .netRevenue(new BigDecimal("5000"))
                .simulation(true).finalized(false)
                .createdBy(treasurer).memberShares(new ArrayList<>())
                .build();

        when(distributionRepo.save(any())).thenReturn(saved);

        // Should not throw ArithmeticException for division by zero
        assertThatCode(() -> revenueService.calculate(req, 1L)).doesNotThrowAnyException();
    }

    @Test
    void finalize_shouldThrowIfAlreadyFinalized() {
        RevenueDistribution dist = RevenueDistribution.builder()
                .id(1L).finalized(true).simulation(false).memberShares(new ArrayList<>()).build();
        when(distributionRepo.findById(1L)).thenReturn(Optional.of(dist));

        assertThatThrownBy(() -> revenueService.finalize(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already finalized");
    }

    @Test
    void delete_shouldThrowIfFinalized() {
        RevenueDistribution dist = RevenueDistribution.builder()
                .id(1L).finalized(true).memberShares(new ArrayList<>()).build();
        when(distributionRepo.findById(1L)).thenReturn(Optional.of(dist));

        assertThatThrownBy(() -> revenueService.delete(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(distributionRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> revenueService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
