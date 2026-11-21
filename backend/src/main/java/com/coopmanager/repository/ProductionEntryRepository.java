package com.coopmanager.repository;

import com.coopmanager.entity.Member;
import com.coopmanager.entity.ProductionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionEntryRepository extends JpaRepository<ProductionEntry, Long> {

    List<ProductionEntry> findByMemberOrderByHarvestDateDesc(Member member);

    List<ProductionEntry> findByStatus(ProductionEntry.ValidationStatus status);

    List<ProductionEntry> findByHarvestDateBetweenOrderByHarvestDateDesc(LocalDate start, LocalDate end);

    List<ProductionEntry> findByMemberAndHarvestDateBetween(Member member, LocalDate start, LocalDate end);

    @Query("SELECT SUM(p.quantityKg) FROM ProductionEntry p WHERE p.status = 'VALIDATED'")
    BigDecimal sumValidatedQuantity();

    @Query("SELECT SUM(p.quantityKg) FROM ProductionEntry p WHERE p.member = :member AND p.status = 'VALIDATED'")
    BigDecimal sumValidatedQuantityByMember(@Param("member") Member member);

    @Query("SELECT SUM(p.quantityKg) FROM ProductionEntry p WHERE p.member = :member AND p.status = 'VALIDATED' AND p.harvestDate BETWEEN :start AND :end")
    BigDecimal sumValidatedQuantityByMemberAndPeriod(@Param("member") Member member, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(p.quantityKg) FROM ProductionEntry p WHERE p.status = 'VALIDATED' AND p.harvestDate BETWEEN :start AND :end")
    BigDecimal sumValidatedQuantityByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT p.productType, SUM(p.quantityKg) FROM ProductionEntry p WHERE p.status = 'VALIDATED' GROUP BY p.productType")
    List<Object[]> sumQuantityByProductType();

    @Query("SELECT EXTRACT(MONTH FROM p.harvestDate), SUM(p.quantityKg) FROM ProductionEntry p WHERE p.status = 'VALIDATED' AND EXTRACT(YEAR FROM p.harvestDate) = :year GROUP BY EXTRACT(MONTH FROM p.harvestDate) ORDER BY 1")
    List<Object[]> monthlyProductionByYear(@Param("year") int year);
}
