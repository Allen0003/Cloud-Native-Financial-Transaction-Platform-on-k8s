package com.transaction.domain.enums;


public enum SettlementStatus {

    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    PENDING("PENDING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    private final String code;

    SettlementStatus(String code) {
        this.code = code;
    }

    // 取得要存入資料庫的字串
    public String getCode() {
        return code;
    }


}
