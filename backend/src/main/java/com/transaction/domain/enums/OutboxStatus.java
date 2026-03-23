package com.transaction.domain.enums;

public enum OutboxStatus {
    NEW("NEW", "待傳送"),

    /**
     * 已成功發送到 Kafka，並收到 Ack 回傳
     */
    SENT("SENT", "已傳送"),

    /**
     * 嘗試發送失敗（例如 Kafka 斷線），等待補發或人工排查
     */
    FAILED("FAILED", "傳送失敗");

    private final String code;
    private final String description;

    OutboxStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
