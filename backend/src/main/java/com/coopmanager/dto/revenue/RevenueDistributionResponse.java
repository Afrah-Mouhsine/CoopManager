package com.coopmanager.dto.revenue;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RevenueDistributionResponse {
    private Long id;
    private String periodLabel;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalRevenue;
    private BigDecimal operatingCosts;
    private BigDecimal netRevenue;
    private boolean simulation;
    private boolean finalized;
    private String createdByName;
    private LocalDateTime createdAt;
    private List<MemberShareResponse> memberShares;

    @Data
    @Builder
    public static class MemberShareResponse {
        private Long memberId;
        private String memberFullName;
        private BigDecimal totalProduction;
        private BigDecimal sharePercentage;
        private BigDecimal shareAmount;
    }
}
