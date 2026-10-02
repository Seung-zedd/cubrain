package com.cubrain.springboot_starter_auth.domain.card.v1.service;

import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardResponseDto;
import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.AnnotationResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfAnnotationService;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfExtractionResultDto;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Offline tests for the Gemini call path (no real API calls).
 * The live check lives in GeminiLiveIntegrationTest and only runs when GEMINI_API_KEY_IT is set.
 */
class FlashcardGeneratorImplTest {

    private static final String MODEL_NOT_FOUND = "NOT_FOUND (code 404) This model models/gemini-2.5-flash is no "
            + "longer available to new users.";

    private FakeChatModel chatModel;
    private PdfAnnotationService pdfAnnotationService;
    private FlashcardGeneratorImpl generator;

    @BeforeEach
    void setUp() {
        chatModel = new FakeChatModel();
        pdfAnnotationService = mock(PdfAnnotationService.class);
        generator = new FlashcardGeneratorImpl(chatModel, new ObjectMapper(), pdfAnnotationService,
                mock(JobManager.class));
    }

    @Test
    void detectsLanguageThenGeneratesCardsPerPage_andParsesFencedJson() throws IOException {
        givenExtraction("이 문서는 한국어로 작성되었습니다.",
                AnnotationResultDto.of(3, "Highlight", "TCP", "TCP is reliable", 0, 0, 0, 0));
        chatModel.thenReturn("Korean");
        chatModel.thenReturn("```json\n[{\"question\":\"Q1\",\"answer\":\"A1\"}]\n```");

        List<FlashcardResponseDto> cards = generator.generateCardsFromPdf(Path.of("dummy.pdf"), UserTier.FREE_USER);

        assertEquals(List.of(FlashcardResponseDto.of("Q1", "A1", 3)), cards);
        assertEquals(2, chatModel.calls.size());
        assertTrue(chatModel.lastUserText().contains("**Target Language:** Korean"));
    }

    @Test
    void retriesOnRateLimitThenSucceeds() throws IOException {
        givenExtraction(null, AnnotationResultDto.of(1, "Underline", "Paris", "capital", 0, 0, 0, 0));
        chatModel.thenThrow("RESOURCE_EXHAUSTED (code 429) Quota exceeded");
        chatModel.thenReturn("[{\"question\":\"Capital of France?\",\"answer\":\"Paris\"}]");

        List<FlashcardResponseDto> cards = generator.generateCardsFromPdf(Path.of("dummy.pdf"), UserTier.GUEST);

        assertEquals(1, cards.size());
        assertEquals(2, chatModel.calls.size());
    }

    @Test
    void modelNotFoundIsNotRetried_andFinalErrorKeepsUpstreamCause() throws IOException {
        givenExtraction("English text",
                AnnotationResultDto.of(1, "Highlight", "a", "ctx", 0, 0, 0, 0),
                AnnotationResultDto.of(2, "Highlight", "b", "ctx", 0, 0, 0, 0));
        chatModel.alwaysThrow(MODEL_NOT_FOUND);

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> generator.generateCardsFromPdf(Path.of("dummy.pdf"), UserTier.FREE_USER));

        // 1 language detection + 1 call per page, no retries on 404
        assertEquals(3, chatModel.calls.size());
        assertTrue(e.getMessage().contains("[MODEL_NOT_FOUND]"), e.getMessage());
        assertTrue(e.getMessage().contains("NOT_FOUND (code 404)"), e.getMessage());
        assertNotNull(e.getCause());
        assertEquals(MODEL_NOT_FOUND, e.getCause().getMessage());
    }

    @Test
    void malformedModelOutputIsReportedAsMalformedResponse() throws IOException {
        givenExtraction(null, AnnotationResultDto.of(1, "Highlight", "a", "ctx", 0, 0, 0, 0));
        chatModel.thenReturn("Sorry, I cannot help with that.");

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> generator.generateCardsFromPdf(Path.of("dummy.pdf"), UserTier.FREE_USER));

        assertTrue(e.getMessage().contains("[MALFORMED_RESPONSE]"), e.getMessage());
    }

    @Test
    void classifiesCommonUpstreamFailures() {
        assertEquals("MODEL_NOT_FOUND", FlashcardGeneratorImpl.classifyFailure(new RuntimeException(MODEL_NOT_FOUND)));
        assertEquals("AUTHENTICATION", FlashcardGeneratorImpl.classifyFailure(
                new RuntimeException("INVALID_ARGUMENT (code 400) API key not valid. Please pass a valid API key.")));
        assertEquals("AUTHENTICATION", FlashcardGeneratorImpl.classifyFailure(
                new RuntimeException("PERMISSION_DENIED (code 403) Method doesn't allow unregistered callers")));
        assertEquals("RATE_LIMIT", FlashcardGeneratorImpl.classifyFailure(
                new RuntimeException("RESOURCE_EXHAUSTED (code 429) Quota exceeded")));
        assertEquals("TIMEOUT", FlashcardGeneratorImpl.classifyFailure(new RuntimeException(
                "An error occurred when calling the Gemini API endpoint.", new InterruptedIOException("timeout"))));
        assertEquals("INVALID_REQUEST", FlashcardGeneratorImpl.classifyFailure(
                new RuntimeException("INVALID_ARGUMENT (code 400) Unsupported parameter")));
        assertEquals("UNKNOWN", FlashcardGeneratorImpl.classifyFailure(new IllegalStateException("boom")));
    }

    private void givenExtraction(String detectionText, AnnotationResultDto... annotations) throws IOException {
        when(pdfAnnotationService.extractAnnotations(any(Path.class), anyInt()))
                .thenReturn(PdfExtractionResultDto.of(List.of(annotations), detectionText, false));
    }

    /** Scripted ChatLanguageModel: records every call and replays queued responses/failures. */
    private static class FakeChatModel implements ChatLanguageModel {
        private final List<List<ChatMessage>> calls = new ArrayList<>();
        private final Deque<Supplier<Response<AiMessage>>> script = new ArrayDeque<>();
        private String alwaysThrow;

        void thenReturn(String text) {
            script.add(() -> Response.from(AiMessage.from(text)));
        }

        void thenThrow(String message) {
            script.add(() -> {
                throw new RuntimeException(message);
            });
        }

        void alwaysThrow(String message) {
            this.alwaysThrow = message;
        }

        String lastUserText() {
            List<ChatMessage> last = calls.get(calls.size() - 1);
            return ((UserMessage) last.get(last.size() - 1)).singleText();
        }

        @Override
        public Response<AiMessage> generate(List<ChatMessage> messages) {
            calls.add(messages);
            if (alwaysThrow != null) {
                throw new RuntimeException(alwaysThrow);
            }
            return script.removeFirst().get();
        }
    }
}
