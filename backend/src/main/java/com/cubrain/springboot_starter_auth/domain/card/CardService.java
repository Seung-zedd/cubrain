package com.cubrain.springboot_starter_auth.domain.card;

import com.cubrain.springboot_starter_auth.domain.pdf.AnnotationResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.PdfAnnotationService;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper; // Spring's default JSON parser
    private final PdfAnnotationService pdfAnnotationService;

    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext) {
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

    public List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier) {
        try {
            // 1. Extract annotations
            List<AnnotationResultDto> annotations = pdfAnnotationService.extractAnnotations(file);

            // 2. Filter based on User Tier
            if (userTier == UserTier.GUEST) {
                annotations = annotations.stream()
                        .filter(a -> a.pageIndex() <= 3)
                        .toList();
            }

            List<FlashcardResponseDto> flashcards = new ArrayList<>();

            // 3. Generate Flashcards for each annotation
            // TODO: Optimize this to batch requests or use async processing for better
            // performance
            for (AnnotationResultDto annotation : annotations) {
                // For PDF annotations, local context is the annotation text itself, global
                // context is omitted for now or could be the page text if available
                FlashcardResponseDto card = generateCardDemo(annotation.text(), annotation.text(),
                        "Extracted from PDF Page " + annotation.pageIndex());
                flashcards.add(card);
            }

            return flashcards;

        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF file", e);
        }
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