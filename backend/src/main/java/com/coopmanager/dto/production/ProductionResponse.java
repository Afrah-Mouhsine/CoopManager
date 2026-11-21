package com.coopmanager.dto.production;

import com.coopmanager.entity.ProductionEntry.ValidationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductionResponse {
    private Long id;
    private Long memberId;
    private String memberFullName;
    private String productType;
    private BigDecimal quantityKg;
    private LocalDate harvestDate;
    private String lotNumber;
    private String notes;
    private ValidationStatus status;
    private String validatedByName;
    private LocalDateTime validatedAt;
    private LocalDateTime createdAt;
}
