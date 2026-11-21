package com.coopmanager.repository;

import com.coopmanager.entity.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    List<FinancialTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(LocalDate start, LocalDate end);

    List<FinancialTransaction> findByTypeOrderByTransactionDateDesc(FinancialTransaction.TransactionType type);

    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE t.type = 'INCOME' AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumIncomeByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE t.type = 'EXPENSE' AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumExpenseByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT EXTRACT(MONTH FROM t.transactionDate), t.type, SUM(t.amount) FROM FinancialTransaction t WHERE EXTRACT(YEAR FROM t.transactionDate) = :year GROUP BY EXTRACT(MONTH FROM t.transactionDate), t.type ORDER BY 1")
    List<Object[]> monthlyFinancialSummary(@Param("year") int year);

    @Query("SELECT t.category, SUM(t.amount) FROM FinancialTransaction t WHERE t.type = 'EXPENSE' AND t.transactionDate BETWEEN :start AND :end GROUP BY t.category")
    List<Object[]> expensesByCategory(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
