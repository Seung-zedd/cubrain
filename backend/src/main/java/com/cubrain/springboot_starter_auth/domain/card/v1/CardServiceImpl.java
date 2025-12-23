package com.cubrain.springboot_starter_auth.domain.card.v1;

import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.AnnotationResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfExtractionResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfAnnotationService;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.cubrain.springboot_starter_auth.domain.user.UserTier.FREE_USER;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

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
            PdfExtractionResultDto extractionResult = pdfAnnotationService.extractAnnotations(file);
            List<AnnotationResultDto> annotations = extractionResult.annotations();
            String firstPageText = extractionResult.detectionText();

            String targetLanguage = detectLanguage(firstPageText);
            log.debug("Detected Language for PDF {}: {}", file.getOriginalFilename(), targetLanguage);

            // Filter annotations based on UserTier (Enforce limits by filtering instead of rejecting)
            int pageLimit = (userTier == FREE_USER) ? 50 : 10;
            annotations = annotations.stream()
                    .filter(a -> a.pageIndex() <= pageLimit)
                    .toList();

            List<FlashcardResponseDto> flashcards = new ArrayList<>();
            int total = annotations.size();

            for (int i = 0; i < total; i++) {
                AnnotationResultDto annotation = annotations.get(i);

                if (userTier == FREE_USER) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                FlashcardResponseDto card = generateCardDemo(annotation.text(), annotation.context(),
                        "Extracted from PDF Page " + annotation.pageIndex(), annotation.type(), targetLanguage);
                flashcards.add(card);

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

            Response<AiMessage> response = chatModel.generate(systemMessage, userMessage);
            return response.content().text().trim();
        } catch (Exception e) {
            log.warn("Language detection failed, falling back to auto-detect", e);
            return "the SAME language as the Target Text";
        }
    }
}
