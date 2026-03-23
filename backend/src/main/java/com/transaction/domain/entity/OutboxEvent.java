package com.transaction.domain.entity;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;  // 關聯的 ID (例如交易序號)
    private String eventType;    // 事件類型 (例如 "SETTLEMENT_CREATED")

    @Column(columnDefinition = "json")
    private String payload;      // 真正要發給 Kafka 的內容 (轉成 JSON 字串)

    private String status = "NEW"; // 狀態：NEW (待傳送), SENT (已傳送)

    private LocalDateTime createdAt = LocalDateTime.now();

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
