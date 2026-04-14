package com.transaction.ai;

import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.SystemMessage;

@AiService
public interface FinancialAgent {

    @SystemMessage("""
        你是一個專業的金融科技助理。
        你的權限可以訪問後台交易系統。
        
        當用戶詢問交易狀態時：
        1. 使用 getTransactionDetails 工具獲取數據。
        2. 如果交易狀態為 FAILED，請根據返回的錯誤訊息提供專業的排查建議。
        3. 請一律使用「繁體中文」回答，保持專業且親切的口吻。
        """)
    String ask(String userMessage);
}
