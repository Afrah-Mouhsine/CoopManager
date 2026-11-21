package com.coopmanager.repository;

import com.coopmanager.entity.RevenueDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevenueDistributionRepository extends JpaRepository<RevenueDistribution, Long> {

    List<RevenueDistribution> findBySimulationFalseAndFinalizedTrueOrderByPeriodStartDesc();

    List<RevenueDistribution> findBySimulationTrueOrderByCreatedAtDesc();

    List<RevenueDistribution> findAllByOrderByPeriodStartDesc();
}
