package com.coopmanager.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class FinancialSummaryDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Map<String, BigDecimal> expensesByCategory;
    private List<MonthlyFinancialPoint> monthlyTrend;

    @Data
    @Builder
    public static class MonthlyFinancialPoint {
        private int month;
        private String monthLabel;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }
}
