package com.cubrain.springboot_starter_auth.domain.card.v1.service;

import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardResponseDto;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardGeneratorImpl implements FlashcardGenerator {

    private static final int BATCH_SIZE = 5;

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;
    private final PdfAnnotationService pdfAnnotationService;
    private final JobManager jobManager;

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
            int pageLimit = switch (userTier) {
                case PRO_USER -> 1000;
                case FREE_USER -> 50;
                case GUEST -> 10;
            };

            int annotationLimit = switch (userTier) {
                case PRO_USER -> 500;
                case FREE_USER -> 50;
                case GUEST -> 50;
            };

            PdfExtractionResultDto extractionResult = pdfAnnotationService.extractAnnotations(file, pageLimit);
            List<AnnotationResultDto> allAnnotations = extractionResult.annotations();

            // Limit annotations to prevent token skyrocketing
            List<AnnotationResultDto> annotations = allAnnotations.size() > annotationLimit
                    ? allAnnotations.subList(0, annotationLimit)
                    : allAnnotations;

            if (allAnnotations.size() > annotationLimit) {
                log.warn("Limiting annotations from {} to {} for user tier {}", allAnnotations.size(), annotationLimit,
                        userTier);
                if (jobId != null) {
                    jobManager.updateMetadata(jobId, "isAnnotationLimited", true);
                    jobManager.updateMetadata(jobId, "annotationLimit", annotationLimit);
                }
            }

            String firstPageText = extractionResult.detectionText();

            String targetLanguage = detectLanguage(firstPageText);
            log.debug("Detected Language for PDF {}: {}", file.getOriginalFilename(), targetLanguage);

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

            // 1. Group annotations by pageIndex to process them contextually
            Map<Integer, List<AnnotationResultDto>> groupedByPage = annotations.stream()
                    .collect(Collectors.groupingBy(AnnotationResultDto::pageIndex, TreeMap::new, Collectors.toList()));

            List<Integer> pageIndices = new ArrayList<>(groupedByPage.keySet());
            int totalPagesWithAnnotations = pageIndices.size();

            log.info("📚 Grouped {} annotations into {} pages for processing", annotations.size(),
                    totalPagesWithAnnotations);

            for (int i = 0; i < totalPagesWithAnnotations; i++) {
                int pageIndex = pageIndices.get(i);
                List<AnnotationResultDto> pageAnnotations = groupedByPage.get(pageIndex);

                log.info("📄 Processing Page {} ({} annotations) - {}/{}",
                        pageIndex, pageAnnotations.size(), i + 1, totalPagesWithAnnotations);

                List<FlashcardResponseDto> pageCards = generateCardsForPage(pageIndex, pageAnnotations, targetLanguage,
                        userTier);
                allFlashcards.addAll(pageCards);

                if (jobId != null) {
                    int progress = (int) (((double) (i + 1) / totalPagesWithAnnotations) * 100);
                    jobManager.updateProgress(jobId, progress);
                }

                // Rate limiting / Token defense delay
                if (i < totalPagesWithAnnotations - 1) {
                    try {
                        Thread.sleep(800);
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

    private List<FlashcardResponseDto> generateCardsForPage(int pageIndex, List<AnnotationResultDto> annotations,
            String targetLanguage, UserTier userTier) {
        try {
            // Simplify annotations to save tokens
            List<Map<String, String>> simplifiedAnnotations = annotations.stream()
                    .map(a -> Map.of(
                            "type", a.type(),
                            "text", a.text(),
                            "context", a.context()))
                    .toList();

            String annotationsJson = objectMapper.writeValueAsString(simplifiedAnnotations);

            SystemMessage systemMessage = SystemMessage
                    .from("You are an expert tutor. Create high-quality Anki flashcards by synthesizing the provided annotations from a single PDF page.");

            String tierInstruction = switch (userTier) {
                case PRO_USER ->
                    "Generate detailed flashcards for each significant annotation. You can create up to 5 cards if the content is rich.";
                default ->
                    "Synthesize these annotations into 1-2 high-quality flashcards that capture the most critical concepts. Quality over quantity.";
            };

            UserMessage userMessage = UserMessage
                    .from("""
                            **Page Context (Page %d):**
                            The user has marked several parts of this page for study.

                            **Annotations (JSON):**
                            %s

                            **Target Language:** %s

                            **Instructions:**
                            1. Analyze the annotations within their provided contexts.
                            2. %s
                            3. Logic:
                               - 'Highlight' -> Conceptual questions (Why, How, Significance).
                               - 'Underline' -> Factual questions (What, Who, When, Definitions).
                            4. If multiple annotations relate to the same concept, merge them into one comprehensive card.
                            5. Return a **JSON ARRAY** of objects.

                            **Output Format:**
                            Strictly return ONLY the JSON Array:
                            [
                              { "question": "...", "answer": "..." }
                            ]
                            """
                            .formatted(pageIndex, annotationsJson, targetLanguage, tierInstruction));

            Response<AiMessage> response = generateWithRetry(systemMessage, userMessage);
            String responseText = response.content().text();
            log.info("🤖 AI Page Response (Page {}): {}", pageIndex, responseText);

            String cleanJson = responseText.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJson, new TypeReference<List<FlashcardResponseDto>>() {
            });
        } catch (Exception e) {
            log.error("Failed to process page {} or parse AI response", pageIndex, e);
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
        return chatModel.generate(systemMessage, userMessage);
    }
}
