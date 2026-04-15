package com.transaction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private String transactionId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;

    public TransactionResponse(String transactionId, Long userId, BigDecimal amount,
                               String currency, String status, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }

    // getters only (immutable response)
    public String getTransactionId() {
        return transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", userId=" + userId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
