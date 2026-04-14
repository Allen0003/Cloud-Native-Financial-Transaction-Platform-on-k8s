package com.transaction.consumer;

import com.transaction.ai.FinancialAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private FinancialAgent financialAgent;

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {

        System.out.println("QQQQQ");

        return financialAgent.ask(message);
    }

}
