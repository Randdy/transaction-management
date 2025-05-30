package com.hsbc.wpb.model;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransactionUpdateRequest {
    @NotNull(message = "From account cannot be null")
    private String fromAccount;

    @NotNull(message = "To account cannot be null")
    private String toAccount;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;

    public TransactionUpdateRequest() {
    }

    public TransactionUpdateRequest(String fromAccount, String toAccount, BigDecimal amount, TransactionType type) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.type = type;
    }
} 