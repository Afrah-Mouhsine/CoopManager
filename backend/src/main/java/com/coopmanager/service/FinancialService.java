package com.coopmanager.service;

import com.coopmanager.dto.finance.FinancialSummaryDTO;
import com.coopmanager.dto.finance.TransactionRequest;
import com.coopmanager.dto.finance.TransactionResponse;
import com.coopmanager.entity.FinancialTransaction;
import com.coopmanager.entity.FinancialTransaction.TransactionType;
import com.coopmanager.entity.Member;
import com.coopmanager.repository.FinancialTransactionRepository;
import com.coopmanager.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialService {

    private final FinancialTransactionRepository transactionRepo;
    private final MemberRepository memberRepository;

    @Transactional
    public TransactionResponse create(TransactionRequest request, Long recorderId) {
        Member recorder = memberRepository.findById(recorderId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        FinancialTransaction tx = FinancialTransaction.builder()
                .type(request.getType())
                .category(request.getCategory())
                .description(request.getDescription())
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .reference(request.getReference())
                .notes(request.getNotes())
                .recordedBy(recorder)
                .build();

        return toResponse(transactionRepo.save(tx));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().withDayOfYear(1);
        if (to == null) to = LocalDate.now();
        return transactionRepo.findByTransactionDateBetweenOrderByTransactionDateDesc(from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        FinancialTransaction tx = getOrThrow(id);
        tx.setType(request.getType());
        tx.setCategory(request.getCategory());
        tx.setDescription(request.getDescription());
        tx.setAmount(request.getAmount());
        tx.setTransactionDate(request.getTransactionDate());
        tx.setReference(request.getReference());
        tx.setNotes(request.getNotes());
        return toResponse(transactionRepo.save(tx));
    }

    @Transactional
    public void delete(Long id) {
        transactionRepo.delete(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public FinancialSummaryDTO getSummary(int year) {
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        BigDecimal income = transactionRepo.sumIncomeByPeriod(yearStart, yearEnd);
        BigDecimal expenses = transactionRepo.sumExpenseByPeriod(yearStart, yearEnd);
        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;

        List<Object[]> expByCat = transactionRepo.expensesByCategory(yearStart, yearEnd);
        Map<String, BigDecimal> expensesByCategory = new LinkedHashMap<>();
        for (Object[] row : expByCat) {
            expensesByCategory.put(row[0].toString(), (BigDecimal) row[1]);
        }

        List<Object[]> monthly = transactionRepo.monthlyFinancialSummary(year);
        Map<Integer, FinancialSummaryDTO.MonthlyFinancialPoint> monthMap = new LinkedHashMap<>();

        for (Object[] row : monthly) {
            int month = ((Number) row[0]).intValue();
            TransactionType type = TransactionType.valueOf((String) row[1]);
            BigDecimal amount = (BigDecimal) row[2];

            monthMap.computeIfAbsent(month, m -> FinancialSummaryDTO.MonthlyFinancialPoint.builder()
                    .month(m)
                    .monthLabel(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.FRENCH))
                    .income(BigDecimal.ZERO)
                    .expenses(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());

            FinancialSummaryDTO.MonthlyFinancialPoint point = monthMap.get(month);
            if (type == TransactionType.INCOME) {
                point.setIncome(amount);
            } else {
                point.setExpenses(amount);
            }
            point.setNet(point.getIncome().subtract(point.getExpenses()));
        }

        return FinancialSummaryDTO.builder()
                .totalIncome(income)
                .totalExpenses(expenses)
                .netBalance(income.subtract(expenses))
                .expensesByCategory(expensesByCategory)
                .monthlyTrend(new ArrayList<>(monthMap.values()))
                .build();
    }

    private FinancialTransaction getOrThrow(Long id) {
        return transactionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + id));
    }

    private TransactionResponse toResponse(FinancialTransaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .category(tx.getCategory())
                .description(tx.getDescription())
                .amount(tx.getAmount())
                .transactionDate(tx.getTransactionDate())
                .reference(tx.getReference())
                .notes(tx.getNotes())
                .recordedByName(tx.getRecordedBy().getFullName())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
