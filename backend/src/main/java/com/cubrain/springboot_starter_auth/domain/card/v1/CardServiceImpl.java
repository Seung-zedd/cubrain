package com.cubrain.springboot_starter_auth.domain.card.v1;

import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.AnnotationResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfExtractionResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfAnnotationService;
import com.cubrain.springboot_starter_auth.domain.member.Member;
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

import static com.cubrain.springboot_starter_auth.domain.user.UserTier.FREE_USER;
import static com.cubrain.springboot_starter_auth.domain.user.UserTier.GUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;
    private final PdfAnnotationService pdfAnnotationService;
    private final JobManager jobManager;
    private final DeckRepository deckRepository;

    @Qualifier("pdfProcessingExecutor")
    private final Executor pdfProcessingExecutor;

    @Override
    public String generateCardsAsync(MultipartFile file, UserTier userTier, String ownerId) {
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
        UserMessage userMessage = UserMessage.from("""
                **Context:**
                "%s"

                **Target Text (%s):**
                "%s"

                **Instructions:**
                1. IF type is 'Highlight': Conceptual Questions.
                2. IF type is 'Underline': Factual Questions.
                3. OUTPUT LANGUAGE: %s.

                Return ONLY JSON: { "question": "...", "answer": "..." }
                """.formatted(localContext, annotationType, selection, targetLanguage));

        Response<AiMessage> response = chatModel.generate(systemMessage, userMessage);
        return parseResponse(response.content().text());
    }

    @Override
    public DeckResponseDto saveDeck(String title, List<FlashcardResponseDto> cards, Member member) {
        Deck deck = Deck.builder()
                .title(title)
                .member(member)
                .build();

        List<Flashcard> flashcards = cards.stream()
                .map(dto -> Flashcard.builder()
                        .deck(deck)
                        .question(dto.question())
                        .answer(dto.answer())
                        .build())
                .toList();

        deck.getCards().addAll(flashcards);
        Deck savedDeck = deckRepository.save(deck);

        return DeckResponseDto.from(savedDeck, flashcards.size());
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
            String targetLanguage = detectLanguage(extractionResult.detectionText());

            List<FlashcardResponseDto> flashcards = new ArrayList<>();
            for (int i = 0; i < annotations.size(); i++) {
                AnnotationResultDto annotation = annotations.get(i);
                FlashcardResponseDto card = generateCardDemo(annotation.text(), annotation.context(),
                        "PDF Page " + annotation.pageIndex(), annotation.type(), targetLanguage);
                flashcards.add(card);

                if (jobId != null) {
                    int progress = (int) (((double) (i + 1) / annotations.size()) * 100);
                    jobManager.updateProgress(jobId, progress);
                }
            }
            return flashcards;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FlashcardResponseDto parseResponse(String rawResponse) {
        try {
            String cleanJson = rawResponse.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJson, FlashcardResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String detectLanguage(String text) {
        if (text == null || text.isBlank())
            return "English";
        SystemMessage systemMessage = SystemMessage.from("You are a language detector.");
        UserMessage userMessage = UserMessage.from("Identify language: " + text);
        Response<AiMessage> response = chatModel.generate(systemMessage, userMessage);
        return response.content().text().trim();
    }
}
