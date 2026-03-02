package com.transaction.controller;

import com.transaction.dto.request.CreateTransactionRequest;
import com.transaction.dto.response.TransactionResponse;
import com.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestBody CreateTransactionRequest request) {

        TransactionResponse tx = service.createTransaction(request);
        return ResponseEntity.ok(tx);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> get(
            @PathVariable String transactionId) {

        return ResponseEntity.ok(service.getTransaction(transactionId));
    }
}
