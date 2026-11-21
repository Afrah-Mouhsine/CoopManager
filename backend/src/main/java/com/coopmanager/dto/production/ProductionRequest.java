package com.coopmanager.dto.production;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductionRequest {

    @NotNull
    private Long memberId;

    @NotBlank
    private String productType;

    @NotNull @Positive
    private BigDecimal quantityKg;

    @NotNull
    private LocalDate harvestDate;

    private String lotNumber;
    private String notes;
}
