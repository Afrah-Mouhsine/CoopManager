package com.coopmanager.service;

import com.coopmanager.dto.production.ProductionRequest;
import com.coopmanager.dto.production.ProductionResponse;
import com.coopmanager.entity.Member;
import com.coopmanager.entity.ProductionEntry;
import com.coopmanager.entity.ProductionEntry.ValidationStatus;
import com.coopmanager.entity.Role;
import com.coopmanager.repository.MemberRepository;
import com.coopmanager.repository.ProductionEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock ProductionEntryRepository productionRepo;
    @Mock MemberRepository memberRepository;
    @InjectMocks ProductionService productionService;

    private Member member;
    private ProductionEntry pendingEntry;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L).firstName("Ahmed").lastName("Benali")
                .email("ahmed@coop.ma").role(Role.MEMBER).active(true).build();

        pendingEntry = ProductionEntry.builder()
                .id(10L).member(member)
                .productType("Olive").quantityKg(new BigDecimal("150.5"))
                .harvestDate(LocalDate.now()).status(ValidationStatus.PENDING)
                .build();
    }

    @Test
    void create_shouldSaveEntryWithPendingStatus() {
        ProductionRequest req = new ProductionRequest();
        req.setMemberId(1L);
        req.setProductType("Olive");
        req.setQuantityKg(new BigDecimal("150.5"));
        req.setHarvestDate(LocalDate.now());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productionRepo.save(any())).thenReturn(pendingEntry);

        ProductionResponse result = productionService.create(req);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PENDING);
        assertThat(result.getQuantityKg()).isEqualByComparingTo("150.5");
        verify(productionRepo).save(any(ProductionEntry.class));
    }

    @Test
    void create_shouldThrowWhenMemberNotFound() {
        ProductionRequest req = new ProductionRequest();
        req.setMemberId(99L);
        req.setProductType("Olive");
        req.setQuantityKg(BigDecimal.TEN);
        req.setHarvestDate(LocalDate.now());

        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productionService.create(req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void validate_shouldSetStatusToValidated() {
        Member treasurer = Member.builder().id(2L).firstName("T").lastName("R")
                .email("tr@coop.ma").role(Role.TREASURER).active(true).build();

        when(productionRepo.findById(10L)).thenReturn(Optional.of(pendingEntry));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(treasurer));
        when(productionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductionResponse result = productionService.validate(10L, 2L);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.VALIDATED);
        assertThat(result.getValidatedByName()).isEqualTo("T R");
    }

    @Test
    void reject_shouldSetStatusToRejected() {
        Member validator = Member.builder().id(2L).firstName("T").lastName("R")
                .email("tr@coop.ma").role(Role.TREASURER).active(true).build();

        when(productionRepo.findById(10L)).thenReturn(Optional.of(pendingEntry));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(validator));
        when(productionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductionResponse result = productionService.reject(10L, 2L);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.REJECTED);
    }

    @Test
    void delete_shouldThrowIfEntryIsValidated() {
        pendingEntry.setStatus(ValidationStatus.VALIDATED);
        when(productionRepo.findById(10L)).thenReturn(Optional.of(pendingEntry));

        assertThatThrownBy(() -> productionService.delete(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("validated");
    }

    @Test
    void delete_shouldSucceedForPendingEntry() {
        when(productionRepo.findById(10L)).thenReturn(Optional.of(pendingEntry));

        assertThatCode(() -> productionService.delete(10L)).doesNotThrowAnyException();
        verify(productionRepo).delete(pendingEntry);
    }
}
