package com.cubrain.springboot_starter_auth.domain.card.service;

import com.cubrain.springboot_starter_auth.domain.card.dto.FlashcardResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// @Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper; // Spring's default JSON parser

    public FlashcardResponseDto generateCard(String selection, String localContext, String globalContext) {
        // 1. The "Senior Tutor" Prompt
        // We ask for strict JSON format to avoid parsing errors.
        SystemMessage systemMessage = SystemMessage.from("You are an expert tutor creating study materials.");
        UserMessage userMessage = UserMessage.from("""
                TASK:
                Create a flashcard (Question and Answer) based on the user's selected text.

                GLOBAL CONTEXT (The full document):
                "%s"

                LOCAL CONTEXT (The specific paragraph/section):
                "%s"

                USER SELECTION (Focus on this):
                "%s"

                RULES:
                1. The 'question' should be engaging and test the core concept in the SELECTION.
                2. Use the LOCAL CONTEXT to understand the immediate meaning.
                3. Use the GLOBAL CONTEXT only for broader disambiguation if needed.
                4. The 'answer' should be concise and accurate.
                5. OUTPUT MUST BE RAW JSON ONLY. No markdown blocks (```json), no explanations.

                JSON FORMAT:
                {
                  "question": "...",
                  "answer": "..."
                }
                """.formatted(globalContext, localContext, selection));

        // 2. Call Gemini
        Response<AiMessage> response = chatModel.generate(systemMessage, userMessage);
        String responseText = response.content().text();
        log.info("🤖 Raw AI Response: " + responseText); // Debug log

        // 3. Clean & Parse JSON
        return parseResponse(responseText);
    }

    private FlashcardResponseDto parseResponse(String rawResponse) {
        try {
            // Remove markdown code blocks if Gemini adds them (e.g., ```json ... ```)
            String cleanJson = rawResponse.replace("```json", "").replace("```", "").trim();

            // Convert String -> Java Object
            return objectMapper.readValue(cleanJson, FlashcardResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage());
        }
    }
}