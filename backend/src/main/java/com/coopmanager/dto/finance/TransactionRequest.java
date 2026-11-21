package com.coopmanager.dto.finance;

import com.coopmanager.entity.FinancialTransaction.TransactionCategory;
import com.coopmanager.entity.FinancialTransaction.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotNull
    private TransactionType type;

    @NotNull
    private TransactionCategory category;

    @NotBlank
    private String description;

    @NotNull @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate transactionDate;

    private String reference;
    private String notes;
}
