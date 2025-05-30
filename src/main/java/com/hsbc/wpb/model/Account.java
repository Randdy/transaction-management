package com.hsbc.wpb.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Account(
    String id,
    String accountNumber,
    String accountHolder,
    BigDecimal balance,
    AccountType type,
    LocalDateTime createdAt,
    AccountStatus status
) {
    public static Account create(String accountNumber, String accountHolder, BigDecimal initialBalance, AccountType type) {
        return new Account(
            UUID.randomUUID().toString(),
            accountNumber,
            accountHolder,
            initialBalance,
            type,
            LocalDateTime.now(),
            AccountStatus.ACTIVE
        );
    }

    public Account withBalance(BigDecimal newBalance) {
        return new Account(
            this.id,
            this.accountNumber,
            this.accountHolder,
            newBalance,
            this.type,
            this.createdAt,
            this.status
        );
    }
}