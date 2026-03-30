//package com.transaction.consumer;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.transaction.domain.entity.SettlementItem;
//import com.transaction.domain.enums.SettlementStatus;
//import com.transaction.domain.repository.SettlementItemRepository;
//import com.transaction.dto.event.SettlementEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.dao.OptimisticLockingFailureException;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Random;
//
//
//@Slf4j
//@Component
//public class SettlementConsumerMultiThread {
//    private static final Logger log = LoggerFactory.getLogger(SettlementConsumerMultiThread.class);
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private SettlementItemRepository repository;
//
//    @Autowired
//    SettlementItemRepository itemRepo;
//
//    @Value("${app.pod.name}")
//    private String podName;
//
//    private final Random random = new Random();
//
//    // 使用 concurrency 屬性實現 Pod 內多執行緒
//    @KafkaListener(
//            topics = "settlement-events",
//            groupId = "settlement-group-v2",
//            concurrency = "3" // 啟動 3 個執行緒並行消費
//    )
//    public void consume(String message, Acknowledgment ack) {
//        log.info("📥 [Thread-ID: {}] [Thread-Name: {}] Received message: {}",
//                Thread.currentThread().getId(), Thread.currentThread().getName(), message);
//
//        try {
//            // 1. 模擬銀行 API 調用 (I/O Bound)
//            Thread.sleep(5000);
//
//            // 2. 業務處理與資料庫更新
//            processSettlement(message, Thread.currentThread().getId());
//
//
//            // 3. 成功後手動提交 Offset
//            ack.acknowledge();
//            log.info("Offset {} 處理成功並已提交");
//
//        } catch (OptimisticLockingFailureException e) {
//            log.error("偵測到併發衝突，Offset {} 將不提交並觸發重試");
//            // 不執行 ack.acknowledge()，Kafka 會根據配置重試
//        } catch (Exception e) {
//            log.error("處理失敗: {}", e.getMessage());
//        }
//    }
//
//    @Transactional
//    private void processSettlement(String message, long threadId) throws Exception {
//        // 1. 解析 JSON
//        SettlementEvent event = objectMapper.readValue(message, SettlementEvent.class);
//
//        // 2. 執行真正的業務邏輯 (例如呼叫外部銀行 API 或更新帳務)
//        SettlementItem item = itemRepo.findById(event.getId()).orElseThrow();
//        item.setProcessed_by(threadId + " podname: " + podName);
//
//        boolean isSuccess = simulateExternalCall();
//
//        if (isSuccess) {
//            item.setStatus(SettlementStatus.SUCCESS.getCode());
//        } else {
//            item.setStatus(SettlementStatus.FAILED.getCode());
//        }
//        itemRepo.save(item);
//    }
//
//    public boolean simulateExternalCall() {
//        // 80% 成功，20% 失敗
//        return random.nextInt(10) < 8;
//    }
//
//}
