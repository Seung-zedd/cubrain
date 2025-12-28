package com.cubrain.springboot_starter_auth.global.config.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Value("${langchain4j.google-ai-gemini.chat-model.api-key}")
    private String chatApiKey;

    @Value("${langchain4j.google-ai-gemini.chat-model.model-name}")
    private String chatModelName;

    @Value("${langchain4j.google-ai-gemini.chat-model.temperature}")
    private Double chatTemperature;

    @Value("${langchain4j.google-ai-gemini.embedding-model.api-key}")
    private String embeddingApiKey;

    @Value("${langchain4j.google-ai-gemini.embedding-model.model-name}")
    private String embeddingModelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(chatApiKey)
                .modelName(chatModelName)
                .temperature(chatTemperature)
                .timeout(Duration.ofSeconds(300))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(embeddingApiKey)
                .modelName(embeddingModelName)
                .build();
    }
}
