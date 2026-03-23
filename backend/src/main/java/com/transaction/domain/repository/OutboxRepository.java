package com.transaction.domain.repository;

import com.transaction.domain.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    // 之後可以用這個找出所有狀態為 "NEW" 的事件來補發
    List<OutboxEvent> findByStatus(String status);
    List<OutboxEvent> findTop10ByStatus(String status);
}
