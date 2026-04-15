package com.transaction.ai;

import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.SystemMessage;

@AiService
public interface FinancialAgent {

    @SystemMessage("""
        你是一個專業的金融科技助理。
        請一律使用「繁體中文」回答用戶的問題。
        保持專業且親切的口吻。
        """)
    String ask(String userMessage);
}
