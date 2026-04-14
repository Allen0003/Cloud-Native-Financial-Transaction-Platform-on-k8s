package com.transaction.ai;

import com.transaction.dto.response.TransactionResponse;
import com.transaction.service.TransactionService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionTools {

    private  TransactionService transactionService;

    @Tool("根據交易 ID (transactionId) 查詢詳細資訊")
    public TransactionResponse getTransactionDetails(String transactionId) {

        System.out.println("i am here ~~~");

        return transactionService.getTransaction(transactionId);
    }
}
