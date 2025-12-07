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
        // Default to "Highlight" behavior for manual demo
        return generateCardDemo(selection, localContext, globalContext, "Highlight");
    }

    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
            String annotationType) {
        // 1. The "Senior Tutor" Prompt
        SystemMessage systemMessage = SystemMessage.from("You are an expert tutor creating Anki flashcards.");

        UserMessage userMessage = UserMessage.from("""
                **Context:**
                "%s"

                **Target Text (%s):**
                "%s"

                **Instructions:**
                1. IF type is 'Highlight':
                   - Focus on the 'Big Picture'. Ask about causes, effects, or core concepts.
                   - Example Q: "Explain the significance of [Topic]."

                2. IF type is 'Underline':
                   - Focus on 'Specific Facts'. Ask for definitions, dates, names, or values.
                   - Use Cloze Deletion format ({{c1::answer}}) if appropriate.
                   - Example Q: "The capital of France is {{c1::Paris}}."

                Based on the Target Text and Context above, generate 1 Flashcard (Question & Answer).
                Return ONLY JSON format: { "question": "...", "answer": "..." }
                """.formatted(localContext, annotationType, selection));

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
            } else if (userTier == UserTier.FREE_USER) {
                // Safety Cap for Free Tier
                if (annotations.size() > 10) {
                    log.warn("⚠️ Free Tier Limit: Processing capped at 10 annotations to prevent timeout.");
                    annotations = annotations.subList(0, 10);
                }
            }

            List<FlashcardResponseDto> flashcards = new ArrayList<>();

            // 3. Generate Flashcards for each annotation
            for (AnnotationResultDto annotation : annotations) {

                // Throttling for FREE_USER to avoid Rate Limits (Batch System)
                if (userTier == UserTier.FREE_USER) {
                    try {
                        Thread.sleep(2000); // 2 seconds delay
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // For PDF annotations, local context is the annotation text itself, global
                // context is omitted for now or could be the page text if available
                FlashcardResponseDto card = generateCardDemo(annotation.text(), annotation.context(),
                        "Extracted from PDF Page " + annotation.pageIndex(), annotation.type());
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