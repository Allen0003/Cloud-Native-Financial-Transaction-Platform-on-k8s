package com.transaction.controller;

import com.transaction.service.SettlementKafkaService;
import com.transaction.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/batch")
public class BatchController {

    @Autowired
    SettlementService settlementService;

    @Autowired
    SettlementKafkaService settlementKafkaService;

    @PostMapping("/run")
    public String run() {
        settlementService.runBatch(LocalDate.now());
        return "Batch triggered";
    }

    @PostMapping("/runKafka")
    public String runKafka() {
        settlementKafkaService.runBatch(LocalDate.now());
        return "Batch triggered";
    }

}
