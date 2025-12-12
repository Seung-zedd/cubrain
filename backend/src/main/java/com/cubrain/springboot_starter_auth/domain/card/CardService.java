package com.cubrain.springboot_starter_auth.domain.card;

import com.cubrain.springboot_starter_auth.domain.job.JobManager;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.cubrain.springboot_starter_auth.domain.user.UserTier.FREE_USER;
import static com.cubrain.springboot_starter_auth.domain.user.UserTier.GUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper; // Spring's default JSON parser
    private final PdfAnnotationService pdfAnnotationService;
    private final JobManager jobManager;
    @org.springframework.beans.factory.annotation.Qualifier("pdfProcessingExecutor")
    private final Executor pdfProcessingExecutor;

    public String generateCardsAsync(MultipartFile file, UserTier userTier) {
        String jobId = jobManager.createJob();

        CompletableFuture.runAsync(() -> {
            try {
                List<FlashcardResponseDto> results = generateCardsFromPdf(file, userTier, jobId);
                jobManager.completeJob(jobId, results);
            } catch (Exception e) {
                log.error("Async job failed", e);
                jobManager.failJob(jobId);
            }
        }, pdfProcessingExecutor);

        return jobId;
    }

    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext) {
        // Default to "Highlight" behavior for manual demo
        return generateCardDemo(selection, localContext, globalContext, "Highlight");
    }

    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
            String annotationType) {
        // 1. The "Senior Tutor" Prompt
        SystemMessage systemMessage = SystemMessage.from("You are an expert tutor creating Anki flashcards.");

        UserMessage userMessage = UserMessage
                .from("""
                        **Context:**
                        "%s"

                        **Target Text (%s):**
                        "%s"

                        **Instructions:**
                        1. IF type is 'Highlight':
                           - User Psychology: "I understand the big picture here." or "This is the main idea."
                           - Action: Generate Conceptual Questions. Ask 'Why', 'How', or 'Summarize'. Focus on logic, cause-and-effect, and the main argument.
                           - Example Q: "Explain the significance of [Topic]."

                        2. IF type is 'Underline':
                           - User Psychology: "This word is the answer." or "I need to memorize this data."
                           - Action: Generate Factual Questions. Ask for definitions, specific dates, names, or numbers.
                           - Use Cloze Deletion format ({{c1::answer}}) if the text is short.
                           - Example Q: "The capital of France is {{c1::Paris}}."

                        Based on the Target Text and Context above, generate 1 Flashcard (Question & Answer).
                        Return ONLY JSON format: { "question": "...", "answer": "..." }
                        """
                        .formatted(localContext, annotationType, selection));

        // 2. Call Gemini
        Response<AiMessage> response = chatModel.generate(systemMessage, userMessage);
        String responseText = response.content().text();
        log.info("🤖 Raw AI Response: " + responseText); // Debug log

        // 3. Clean & Parse JSON
        return parseResponse(responseText);
    }

    // Overloaded for backward compatibility if needed, but modified to support
    // progress tracking
    public List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier) {
        return generateCardsFromPdf(file, userTier, null);
    }

    public List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier, String jobId) {
        try {
            // 1. Extract annotations
            List<AnnotationResultDto> annotations = pdfAnnotationService.extractAnnotations(file);

            // 2. Filter based on User Tier
            if (userTier == GUEST) {
                annotations = annotations.stream()
                        .filter(a -> a.pageIndex() <= 3)
                        .toList();
            } else if (userTier == FREE_USER) {
                // Safety Cap for Free Tier: Top 10 Highlights + Top 10 Underlines
                List<AnnotationResultDto> highlights = annotations.stream()
                        .filter(a -> "Highlight".equalsIgnoreCase(a.type()))
                        .limit(10)
                        .toList();

                List<AnnotationResultDto> underlines = annotations.stream()
                        .filter(a -> "Underline".equalsIgnoreCase(a.type()))
                        .limit(10)
                        .toList();

                if (annotations.size() > (highlights.size() + underlines.size())) {
                    log.warn(
                            "⚠️ Free Tier Limit: Processing capped at 10 Highlights + 10 Underlines to prevent timeout.");
                }

                annotations = new ArrayList<>();
                annotations.addAll(highlights);
                annotations.addAll(underlines);
            }

            List<FlashcardResponseDto> flashcards = new ArrayList<>();
            int total = annotations.size();

            // 3. Generate Flashcards for each annotation
            for (int i = 0; i < total; i++) {
                AnnotationResultDto annotation = annotations.get(i);

                // Throttling for FREE_USER to avoid Rate Limits (Batch System)
                // NOTE: Using Thread.sleep() for simple rate limiting. For production,
                // consider using reactive approaches, TaskScheduler, or queue-based systems
                // (e.g., RabbitMQ, Redis) to avoid blocking thread pool threads.
                if (userTier == FREE_USER) {
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

                // Update Progress
                if (jobId != null) {
                    int progress = (int) (((double) (i + 1) / total) * 100);
                    jobManager.updateProgress(jobId, progress);
                }
            }

            return flashcards;

        } catch (IOException e) {
            String filename = file.getOriginalFilename();
            log.error("Failed to process PDF file: {} - {}", filename, e.getMessage(), e);
            throw new RuntimeException("Failed to process PDF file: " + filename + " - " + e.getMessage(), e);
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