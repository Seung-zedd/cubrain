package com.cubrain.springboot_starter_auth.domain.card.service;

import com.cubrain.springboot_starter_auth.domain.card.dto.FlashcardResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper; // Spring's default JSON parser

    public FlashcardResponseDto generateCard(String selection, String localContext, String globalContext) {
        // Validate required parameters
        if (selection == null || selection.isBlank()) {
            throw new IllegalArgumentException("Selection text is required and cannot be empty");
        }

        // 1. The "Senior Tutor" Prompt
        // We ask for strict JSON format to avoid parsing errors.
        String prompt = """
                You are an expert tutor creating study materials.

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
                """.formatted(globalContext, localContext, selection);

        // 2. Call Gemini
        String response = chatModel.chat(prompt);
        System.out.println("🤖 Raw AI Response: " + response); // Debug log

        // 3. Clean & Parse JSON
        return parseResponse(response);
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