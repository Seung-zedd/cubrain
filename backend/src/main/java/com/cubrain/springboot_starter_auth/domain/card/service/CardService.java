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

    public FlashcardResponseDto generateCard(String selectedText) {
        // 1. The "Senior Tutor" Prompt
        // We ask for strict JSON format to avoid parsing errors.
        String prompt = """
                You are an expert tutor creating study materials.

                TASK:
                Create a flashcard (Question and Answer) based on the user's selected text.

                USER SELECTION:
                "%s"

                RULES:
                1. The 'question' should be engaging and test the core concept.
                2. The 'answer' should be concise and accurate.
                3. OUTPUT MUST BE RAW JSON ONLY. No markdown blocks (```json), no explanations.

                JSON FORMAT:
                {
                  "question": "...",
                  "answer": "..."
                }
                """.formatted(selectedText);

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