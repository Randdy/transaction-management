package com.hsbc.wpb.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
    Long id,
    String fromAccount,
    String toAccount,
    BigDecimal amount,
    TransactionType type,
    LocalDateTime timestamp,
    TransactionStatus status
) {
    public static Transaction create(String fromAccount, String toAccount, BigDecimal amount, TransactionType type) {
        return new Transaction(
            null,  // ID will be generated by the repository
            fromAccount,
            toAccount,
            amount,
            type,
            LocalDateTime.now(),
            TransactionStatus.PENDING
        );
    }

    // Getters
    public Long getId() { return id; }
    public String getFromAccount() { return fromAccount; }
    public String getToAccount() { return toAccount; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public TransactionStatus getStatus() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }
}