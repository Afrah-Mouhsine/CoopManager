package com.coopmanager.service;

import com.coopmanager.dto.finance.TransactionRequest;
import com.coopmanager.dto.finance.TransactionResponse;
import com.coopmanager.entity.FinancialTransaction;
import com.coopmanager.entity.FinancialTransaction.TransactionCategory;
import com.coopmanager.entity.FinancialTransaction.TransactionType;
import com.coopmanager.entity.Member;
import com.coopmanager.entity.Role;
import com.coopmanager.repository.FinancialTransactionRepository;
import com.coopmanager.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialServiceTest {

    @Mock FinancialTransactionRepository transactionRepo;
    @Mock MemberRepository memberRepository;
    @InjectMocks FinancialService financialService;

    private Member treasurer;
    private FinancialTransaction saleTransaction;

    @BeforeEach
    void setUp() {
        treasurer = Member.builder().id(1L).firstName("Karim").lastName("T")
                .email("karim@coop.ma").role(Role.TREASURER).active(true).build();

        saleTransaction = FinancialTransaction.builder()
                .id(1L).type(TransactionType.INCOME).category(TransactionCategory.SALE)
                .description("Vente olives").amount(new BigDecimal("5000"))
                .transactionDate(LocalDate.now()).recordedBy(treasurer)
                .build();
    }

    @Test
    void create_shouldPersistTransaction() {
        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.INCOME);
        req.setCategory(TransactionCategory.SALE);
        req.setDescription("Vente olives");
        req.setAmount(new BigDecimal("5000"));
        req.setTransactionDate(LocalDate.now());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(treasurer));
        when(transactionRepo.save(any())).thenReturn(saleTransaction);

        TransactionResponse result = financialService.create(req, 1L);

        assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(result.getAmount()).isEqualByComparingTo("5000");
        verify(transactionRepo).save(any(FinancialTransaction.class));
    }

    @Test
    void create_shouldThrowWhenRecorderNotFound() {
        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.INCOME);
        req.setCategory(TransactionCategory.SALE);
        req.setDescription("Test");
        req.setAmount(BigDecimal.TEN);
        req.setTransactionDate(LocalDate.now());

        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.create(req, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldUseCurrentYearWhenNoDatesProvided() {
        when(transactionRepo.findByTransactionDateBetweenOrderByTransactionDateDesc(any(), any()))
                .thenReturn(List.of(saleTransaction));

        List<TransactionResponse> result = financialService.findAll(null, null);

        assertThat(result).hasSize(1);
        verify(transactionRepo).findByTransactionDateBetweenOrderByTransactionDateDesc(any(), any());
    }

    @Test
    void delete_shouldRemoveTransaction() {
        when(transactionRepo.findById(1L)).thenReturn(Optional.of(saleTransaction));

        financialService.delete(1L);

        verify(transactionRepo).delete(saleTransaction);
    }

    @Test
    void update_shouldModifyExistingTransaction() {
        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.EXPENSE);
        req.setCategory(TransactionCategory.INPUTS);
        req.setDescription("Engrais");
        req.setAmount(new BigDecimal("1200"));
        req.setTransactionDate(LocalDate.now());

        when(transactionRepo.findById(1L)).thenReturn(Optional.of(saleTransaction));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse result = financialService.update(1L, req);

        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getAmount()).isEqualByComparingTo("1200");
    }
}
