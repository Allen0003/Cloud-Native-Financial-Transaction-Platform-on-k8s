package com.transaction.service;


import com.transaction.dto.request.CreateTransactionRequest;
import com.transaction.dto.response.TransactionResponse;

public interface TransactionService {
    TransactionResponse createTransaction(CreateTransactionRequest request);

    TransactionResponse getTransaction(String transactionId);
}
