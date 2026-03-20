package com.transaction.dto.event;

import lombok.Data;

@Data
public class SettlementEvent {
    private Long id;
    private String status;
    // 根據你之前放入 Payload 的欄位來定義
}
