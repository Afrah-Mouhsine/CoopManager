package com.coopmanager.dto.production;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductionDashboardDTO {
    private BigDecimal totalValidatedKg;
    private long pendingCount;
    private Map<String, BigDecimal> byProductType;
    private List<MonthlyPoint> monthlyTrend;

    @Data
    @Builder
    public static class MonthlyPoint {
        private int month;
        private String monthLabel;
        private BigDecimal totalKg;
    }
}
