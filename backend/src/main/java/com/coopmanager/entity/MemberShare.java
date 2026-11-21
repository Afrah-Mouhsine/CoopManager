package com.coopmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "member_shares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribution_id", nullable = false)
    private RevenueDistribution distribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal totalProduction;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal sharePercentage;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal shareAmount;
}
