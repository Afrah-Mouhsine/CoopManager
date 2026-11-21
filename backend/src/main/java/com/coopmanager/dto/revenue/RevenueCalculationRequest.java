package com.coopmanager.dto.revenue;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RevenueCalculationRequest {

    @NotBlank
    private String periodLabel;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    @NotNull @Positive
    private BigDecimal totalRevenue;

    @NotNull @PositiveOrZero
    private BigDecimal operatingCosts;

    private boolean simulation;
}
