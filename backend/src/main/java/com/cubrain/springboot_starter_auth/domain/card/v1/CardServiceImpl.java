package com.cubrain.springboot_starter_auth.domain.card.v1;

import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.AnnotationResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfExtractionResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfAnnotationService;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final int BATCH_SIZE = 5;

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;
    private final PdfAnnotationService pdfAnnotationService;
    private final JobManager jobManager;

    @Qualifier("pdfProcessingExecutor")
    private final Executor pdfProcessingExecutor;

    @Override
    public String generateCardsAsync(MultipartFile file, UserTier userTier, String ownerId) {
        String jobId = jobManager.createJob(ownerId);

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

    @Override
    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext) {
        return generateCardDemo(selection, localContext, globalContext, "Highlight",
                "the SAME language as the Target Text");
    }

    @Override
    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
            String annotationType) {
        return generateCardDemo(selection, localContext, globalContext, annotationType,
                "the SAME language as the Target Text");
    }

    @Override
    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
            String annotationType, String targetLanguage) {
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
                           - Provide a direct, concise answer.
                           - Example Q: "What is the capital of France?" Answer: "Paris"

                        3. OUTPUT LANGUAGE:
                           - Generate the Question and Answer in %s.

                        Based on the Target Text and Context above, generate 1 Flashcard (Question & Answer).
                        Return ONLY JSON format: { "question": "...", "answer": "..." }
                        """
                        .formatted(localContext, annotationType, selection, targetLanguage));

        Response<AiMessage> response = chatModel.generate(systemMessage, userMessage);
        String responseText = response.content().text();
        log.info("🤖 Raw AI Response: " + responseText);

        return parseResponse(responseText);
    }

    @Override
    public List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier) {
        return generateCardsFromPdf(file, userTier, null);
    }

    @Override
    public List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier, String jobId) {
        try {
            // 1. Determine limits based on UserTier
            int pageLimit = switch (userTier) {
                case PRO_USER -> 1000;
                case FREE_USER -> 50;
                case GUEST -> 10;
            };

            // 2. Extract annotations up to the page limit
            PdfExtractionResultDto extractionResult = pdfAnnotationService.extractAnnotations(file, pageLimit);
            List<AnnotationResultDto> annotations = extractionResult.annotations();
            String firstPageText = extractionResult.detectionText();

            String targetLanguage = detectLanguage(firstPageText);
            log.debug("Detected Language for PDF {}: {}", file.getOriginalFilename(), targetLanguage);

            // Small delay after language detection to avoid rapid-fire
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (extractionResult.isLimited()) {
                if (jobId != null) {
                    jobManager.updateMetadata(jobId, "isLimited", true);
                    jobManager.updateMetadata(jobId, "pageLimit", pageLimit);
                }
            }

            List<FlashcardResponseDto> allFlashcards = new ArrayList<>();
            int total = annotations.size();

            for (int i = 0; i < total; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, total);
                List<AnnotationResultDto> batch = annotations.subList(i, end);

                log.info("Processing batch {} to {} of {}", i + 1, end, total);
                List<FlashcardResponseDto> batchCards = generateCardBatch(batch, targetLanguage);
                allFlashcards.addAll(batchCards);

                if (jobId != null) {
                    int progress = (int) (((double) end / total) * 100);
                    jobManager.updateProgress(jobId, progress);
                }

                // Add a small delay between batches to respect rate limits
                if (end < total) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (allFlashcards.isEmpty() && !annotations.isEmpty()) {
                throw new RuntimeException(
                        "Failed to generate any flashcards from the provided annotations. This might be due to API timeouts or rate limits.");
            }

            return allFlashcards;

        } catch (IOException e) {
            String filename = file.getOriginalFilename();
            log.error("Failed to process PDF file: {} - {}", filename, e.getMessage(), e);
            throw new RuntimeException("Failed to process PDF file: " + filename + " - " + e.getMessage(), e);
        }
    }

    private List<FlashcardResponseDto> generateCardBatch(List<AnnotationResultDto> batch, String targetLanguage) {
        try {
            String batchJson = objectMapper.writeValueAsString(batch);

            SystemMessage systemMessage = SystemMessage
                    .from("You are an expert tutor. Create Anki flashcards from the provided list of annotations.");

            UserMessage userMessage = UserMessage
                    .from("""
                            **Task:**
                            Generate %d flashcards based on the following list of annotations.

                            **Target Language:** %s

                            **Input Data (JSON):**
                            %s

                            **Instructions:**
                            1. Iterate through each item in the input list.
                            2. Apply the logic based on 'type' (Highlight -> Conceptual, Underline -> Factual).
                            3. Return a **JSON ARRAY** of objects.

                            **Output Format:**
                            Strictly return ONLY the JSON Array:
                            [
                              { "question": "...", "answer": "..." },
                              { "question": "...", "answer": "..." }
                            ]
                            """
                            .formatted(batch.size(), targetLanguage, batchJson));

            Response<AiMessage> response = generateWithRetry(systemMessage, userMessage);
            String responseText = response.content().text();
            log.info("🤖 Raw AI Batch Response: " + responseText);

            String cleanJson = responseText.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJson, new TypeReference<List<FlashcardResponseDto>>() {
            });
        } catch (Exception e) {
            log.error("Failed to process batch or parse AI response", e);
            return new ArrayList<>();
        }
    }

    private FlashcardResponseDto parseResponse(String rawResponse) {
        try {
            String cleanJson = rawResponse.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJson, FlashcardResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage());
        }
    }

    private String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return "the SAME language as the Target Text";
        }
        try {
            SystemMessage systemMessage = SystemMessage.from("You are a language detector.");
            UserMessage userMessage = UserMessage.from("""
                    Identify the primary language of the following text.
                    Return ONLY the language name (e.g., English, Korean, French, etc.).
                    Do not add any punctuation or extra words.

                    Text:
                    "%s"
                    """.formatted(text));

            Response<AiMessage> response = generateWithRetry(systemMessage, userMessage);
            return response.content().text().trim();
        } catch (Exception e) {
            log.warn("Language detection failed, falling back to auto-detect", e);
            return "the SAME language as the Target Text";
        }
    }

    private Response<AiMessage> generateWithRetry(SystemMessage systemMessage, UserMessage userMessage) {
        int maxRetries = 3;
        int delayMs = 3000;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return chatModel.generate(systemMessage, userMessage);
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                boolean isRetryable = errorMsg != null && (errorMsg.contains("429") ||
                        errorMsg.contains("RESOURCE_EXHAUSTED") ||
                        errorMsg.toLowerCase().contains("timeout"));

                if (isRetryable) {
                    log.warn("Retryable error occurred: {}. Retrying in {}ms... (Attempt {}/{})",
                            errorMsg, delayMs, i + 1, maxRetries);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                    delayMs *= 2; // Exponential backoff
                } else {
                    throw e;
                }
            }
        }
        return chatModel.generate(systemMessage, userMessage); // Final attempt
    }
}
