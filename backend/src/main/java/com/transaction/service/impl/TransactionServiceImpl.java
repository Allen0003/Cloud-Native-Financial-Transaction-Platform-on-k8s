package com.transaction.service.impl;

import com.transaction.domain.entity.Transaction;
import com.transaction.domain.enums.TransactionStatus;
import com.transaction.domain.repository.TransactionRepository;
import com.transaction.dto.request.CreateTransactionRequest;
import com.transaction.dto.response.TransactionResponse;
import com.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository repository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {

//        我設計了 idempotency key + DB unique constraint，
        // key 怎麼來的 GPT 說前端產生就好
        // 類似這樣 const idempotencyKey = crypto.randomUUID()
        // order ID 才使用 zooKeeper
//就算同時兩個 pod 收到同樣請求，也只會成功一筆。

        // 1️⃣ Idempotency check
        Optional<Transaction> existing =
                repository.findByIdempotencyKey(request.getIdempotencyKey());

        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        // 2️⃣ Create new transaction
        Transaction tx = new Transaction();
//        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setTransactionId(request.getIdempotencyKey());

        tx.setUserId(request.getUserId());
        tx.setAmount(request.getAmount());
        tx.setCurrency(request.getCurrency());
        tx.setStatus(TransactionStatus.PENDING.name());
        tx.setIdempotencyKey(request.getIdempotencyKey());

        Transaction saved = repository.save(tx);

        return mapToResponse(saved);
    }

    @Override
    public TransactionResponse getTransaction(String transactionId) {
        Transaction tx = repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return mapToResponse(tx);
    }


    private TransactionResponse mapToResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getTransactionId(),
                tx.getUserId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getStatus(),
                tx.getCreatedAt()
        );
    }
}
