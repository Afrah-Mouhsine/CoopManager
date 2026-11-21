package com.coopmanager.dto.finance;

import com.coopmanager.entity.FinancialTransaction.TransactionCategory;
import com.coopmanager.entity.FinancialTransaction.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private TransactionCategory category;
    private String description;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String reference;
    private String notes;
    private String recordedByName;
    private LocalDateTime createdAt;
}
