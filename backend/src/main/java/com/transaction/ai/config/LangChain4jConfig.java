package com.transaction.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

public class LangChain4jConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // Ollama 預設埠
                .modelName("qwen2.5:7b")           // 確保你已經 ollama pull qwen2.5
                .temperature(0.3)                  // 金融專案建議低溫度，回覆更穩定
                .timeout(Duration.ofSeconds(60))   // Local 跑模型可能較慢，超時設長一點
                .build();
    }
}
