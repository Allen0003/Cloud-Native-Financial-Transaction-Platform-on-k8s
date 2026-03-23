package com.transaction.dto.event;

import lombok.Data;

@Data
public class SettlementEvent {
    private Long id;
    private String status;
    // 根據你之前放入 Payload 的欄位來定義


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
