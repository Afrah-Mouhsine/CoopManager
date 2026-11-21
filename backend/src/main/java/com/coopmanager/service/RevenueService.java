package com.coopmanager.service;

import com.coopmanager.dto.revenue.RevenueCalculationRequest;
import com.coopmanager.dto.revenue.RevenueDistributionResponse;
import com.coopmanager.dto.revenue.RevenueDistributionResponse.MemberShareResponse;
import com.coopmanager.entity.Member;
import com.coopmanager.entity.MemberShare;
import com.coopmanager.entity.RevenueDistribution;
import com.coopmanager.repository.MemberRepository;
import com.coopmanager.repository.ProductionEntryRepository;
import com.coopmanager.repository.RevenueDistributionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final RevenueDistributionRepository distributionRepo;
    private final ProductionEntryRepository productionRepo;
    private final MemberRepository memberRepository;

    @Transactional
    public RevenueDistributionResponse calculate(RevenueCalculationRequest request, Long creatorId) {
        Member creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        BigDecimal netRevenue = request.getTotalRevenue().subtract(request.getOperatingCosts());

        // Fetch all active members and their validated production for the period
        List<Member> activeMembers = memberRepository.findByActiveTrue();
        BigDecimal totalCoopProduction = productionRepo.sumValidatedQuantityByPeriod(
                request.getPeriodStart(), request.getPeriodEnd());

        if (totalCoopProduction == null || totalCoopProduction.compareTo(BigDecimal.ZERO) == 0) {
            totalCoopProduction = BigDecimal.ONE; // avoid division by zero
        }

        List<MemberShare> shares = new ArrayList<>();
        for (Member member : activeMembers) {
            BigDecimal memberProduction = productionRepo.sumValidatedQuantityByMemberAndPeriod(
                    member, request.getPeriodStart(), request.getPeriodEnd());
            if (memberProduction == null) memberProduction = BigDecimal.ZERO;

            BigDecimal percentage = memberProduction
                    .divide(totalCoopProduction, 6, RoundingMode.HALF_UP);
            BigDecimal amount = netRevenue.multiply(percentage).setScale(2, RoundingMode.HALF_UP);

            shares.add(MemberShare.builder()
                    .member(member)
                    .totalProduction(memberProduction)
                    .sharePercentage(percentage)
                    .shareAmount(amount)
                    .build());
        }

        RevenueDistribution distribution = RevenueDistribution.builder()
                .periodLabel(request.getPeriodLabel())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .totalRevenue(request.getTotalRevenue())
                .operatingCosts(request.getOperatingCosts())
                .netRevenue(netRevenue)
                .simulation(request.isSimulation())
                .finalized(!request.isSimulation())
                .createdBy(creator)
                .build();

        distribution = distributionRepo.save(distribution);

        for (MemberShare share : shares) {
            share.setDistribution(distribution);
        }
        distribution.setMemberShares(shares);
        distribution = distributionRepo.save(distribution);

        return toResponse(distribution);
    }

    @Transactional
    public RevenueDistributionResponse finalize(Long id) {
        RevenueDistribution dist = getOrThrow(id);
        if (dist.isFinalized()) {
            throw new IllegalStateException("Distribution is already finalized");
        }
        dist.setSimulation(false);
        dist.setFinalized(true);
        return toResponse(distributionRepo.save(dist));
    }

    @Transactional(readOnly = true)
    public List<RevenueDistributionResponse> findAll() {
        return distributionRepo.findAllByOrderByPeriodStartDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RevenueDistributionResponse> findFinalized() {
        return distributionRepo.findBySimulationFalseAndFinalizedTrueOrderByPeriodStartDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RevenueDistributionResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public RevenueDistributionResponse findMyShare(Long distributionId, Long memberId) {
        RevenueDistribution dist = getOrThrow(distributionId);
        RevenueDistribution filtered = RevenueDistribution.builder()
                .id(dist.getId())
                .periodLabel(dist.getPeriodLabel())
                .periodStart(dist.getPeriodStart())
                .periodEnd(dist.getPeriodEnd())
                .totalRevenue(dist.getTotalRevenue())
                .operatingCosts(dist.getOperatingCosts())
                .netRevenue(dist.getNetRevenue())
                .finalized(dist.isFinalized())
                .simulation(dist.isSimulation())
                .build();

        List<MemberShare> myShares = dist.getMemberShares().stream()
                .filter(s -> s.getMember().getId().equals(memberId))
                .collect(Collectors.toList());
        filtered.setMemberShares(myShares);
        return toResponse(filtered);
    }

    @Transactional
    public void delete(Long id) {
        RevenueDistribution dist = getOrThrow(id);
        if (dist.isFinalized()) {
            throw new IllegalStateException("Cannot delete a finalized distribution");
        }
        distributionRepo.delete(dist);
    }

    private RevenueDistribution getOrThrow(Long id) {
        return distributionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Distribution not found: " + id));
    }

    private RevenueDistributionResponse toResponse(RevenueDistribution d) {
        List<MemberShareResponse> shareResponses = d.getMemberShares() == null ? List.of() :
                d.getMemberShares().stream()
                        .map(s -> MemberShareResponse.builder()
                                .memberId(s.getMember().getId())
                                .memberFullName(s.getMember().getFullName())
                                .totalProduction(s.getTotalProduction())
                                .sharePercentage(s.getSharePercentage().multiply(BigDecimal.valueOf(100))
                                        .setScale(2, RoundingMode.HALF_UP))
                                .shareAmount(s.getShareAmount())
                                .build())
                        .collect(Collectors.toList());

        return RevenueDistributionResponse.builder()
                .id(d.getId())
                .periodLabel(d.getPeriodLabel())
                .periodStart(d.getPeriodStart())
                .periodEnd(d.getPeriodEnd())
                .totalRevenue(d.getTotalRevenue())
                .operatingCosts(d.getOperatingCosts())
                .netRevenue(d.getNetRevenue())
                .simulation(d.isSimulation())
                .finalized(d.isFinalized())
                .createdByName(d.getCreatedBy() != null ? d.getCreatedBy().getFullName() : null)
                .createdAt(d.getCreatedAt())
                .memberShares(shareResponses)
                .build();
    }
}
