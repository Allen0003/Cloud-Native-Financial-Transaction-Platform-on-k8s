package com.transaction.domain.enums;


import java.util.Set;

public enum TransactionStatus {

    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED;

    public boolean canTransitionTo(TransactionStatus target) {
        return switch (this) {
            case PENDING -> Set.of(SUCCESS, FAILED, CANCELLED).contains(target);
            case SUCCESS, FAILED, CANCELLED -> false;
        };
    }
}
