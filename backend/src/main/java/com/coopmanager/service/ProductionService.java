package com.coopmanager.service;

import com.coopmanager.dto.production.ProductionDashboardDTO;
import com.coopmanager.dto.production.ProductionRequest;
import com.coopmanager.dto.production.ProductionResponse;
import com.coopmanager.entity.Member;
import com.coopmanager.entity.ProductionEntry;
import com.coopmanager.entity.ProductionEntry.ValidationStatus;
import com.coopmanager.repository.MemberRepository;
import com.coopmanager.repository.ProductionEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionEntryRepository productionRepo;
    private final MemberRepository memberRepository;

    @Transactional
    public ProductionResponse create(ProductionRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + request.getMemberId()));

        ProductionEntry entry = ProductionEntry.builder()
                .member(member)
                .productType(request.getProductType())
                .quantityKg(request.getQuantityKg())
                .harvestDate(request.getHarvestDate())
                .lotNumber(request.getLotNumber())
                .notes(request.getNotes())
                .status(ValidationStatus.PENDING)
                .build();

        return toResponse(productionRepo.save(entry));
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> findAll() {
        return productionRepo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> findByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
        return productionRepo.findByMemberOrderByHarvestDateDesc(member).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> findByDateRange(LocalDate start, LocalDate end) {
        return productionRepo.findByHarvestDateBetweenOrderByHarvestDateDesc(start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> findPending() {
        return productionRepo.findByStatus(ValidationStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductionResponse validate(Long id, Long validatorId) {
        ProductionEntry entry = getOrThrow(id);
        Member validator = memberRepository.findById(validatorId)
                .orElseThrow(() -> new EntityNotFoundException("Validator not found"));

        entry.setStatus(ValidationStatus.VALIDATED);
        entry.setValidatedBy(validator);
        entry.setValidatedAt(LocalDateTime.now());
        return toResponse(productionRepo.save(entry));
    }

    @Transactional
    public ProductionResponse reject(Long id, Long validatorId) {
        ProductionEntry entry = getOrThrow(id);
        Member validator = memberRepository.findById(validatorId)
                .orElseThrow(() -> new EntityNotFoundException("Validator not found"));

        entry.setStatus(ValidationStatus.REJECTED);
        entry.setValidatedBy(validator);
        entry.setValidatedAt(LocalDateTime.now());
        return toResponse(productionRepo.save(entry));
    }

    @Transactional
    public void delete(Long id) {
        ProductionEntry entry = getOrThrow(id);
        if (entry.getStatus() == ValidationStatus.VALIDATED) {
            throw new IllegalStateException("Cannot delete a validated production entry");
        }
        productionRepo.delete(entry);
    }

    @Transactional(readOnly = true)
    public ProductionDashboardDTO getDashboard(int year) {
        BigDecimal total = productionRepo.sumValidatedQuantity();

        List<Object[]> byType = productionRepo.sumQuantityByProductType();
        Map<String, BigDecimal> byProductType = new LinkedHashMap<>();
        for (Object[] row : byType) {
            byProductType.put((String) row[0], (BigDecimal) row[1]);
        }

        List<Object[]> monthly = productionRepo.monthlyProductionByYear(year);
        List<ProductionDashboardDTO.MonthlyPoint> trend = monthly.stream()
                .map(row -> {
                    int month = ((Number) row[0]).intValue();
                    return ProductionDashboardDTO.MonthlyPoint.builder()
                            .month(month)
                            .monthLabel(Month.of(month).getDisplayName(TextStyle.SHORT, Locale.FRENCH))
                            .totalKg((BigDecimal) row[1])
                            .build();
                })
                .collect(Collectors.toList());

        long pendingCount = productionRepo.findByStatus(ValidationStatus.PENDING).size();

        return ProductionDashboardDTO.builder()
                .totalValidatedKg(total != null ? total : BigDecimal.ZERO)
                .pendingCount(pendingCount)
                .byProductType(byProductType)
                .monthlyTrend(trend)
                .build();
    }

    private ProductionEntry getOrThrow(Long id) {
        return productionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Production entry not found: " + id));
    }

    public ProductionResponse toResponse(ProductionEntry e) {
        return ProductionResponse.builder()
                .id(e.getId())
                .memberId(e.getMember().getId())
                .memberFullName(e.getMember().getFullName())
                .productType(e.getProductType())
                .quantityKg(e.getQuantityKg())
                .harvestDate(e.getHarvestDate())
                .lotNumber(e.getLotNumber())
                .notes(e.getNotes())
                .status(e.getStatus())
                .validatedByName(e.getValidatedBy() != null ? e.getValidatedBy().getFullName() : null)
                .validatedAt(e.getValidatedAt())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
