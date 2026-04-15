package com.transaction.controller;

import com.transaction.ai.FinancialAgent;
import com.transaction.dto.response.TransactionResponse;
import com.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    @Autowired
    FinancialAgent financialAgent;

    @Autowired
    TransactionService transactionService;

    @GetMapping("/chat")
    public String chat(@RequestParam String tranId, @RequestParam String msg) {

        System.out.println("tranId = " + tranId);
        System.out.println("msg = " + msg);

        TransactionResponse realData = transactionService.getTransaction(tranId);

        String augmentedPrompt = String.format(
                "這是從後台系統查詢到的真實數據：%s\n\n用戶的問題是：%s",
                realData.toString(),
                msg
        );


        System.out.println("msg = " + augmentedPrompt);

//        System.out.println("msg = " + message);
        return financialAgent.ask(augmentedPrompt);
    }

}
