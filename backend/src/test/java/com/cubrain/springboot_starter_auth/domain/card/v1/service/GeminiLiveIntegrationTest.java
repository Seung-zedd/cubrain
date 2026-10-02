package com.cubrain.springboot_starter_auth.domain.card.v1.service;

import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardResponseDto;
import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.AnnotationResultDto;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfAnnotationService;
import com.cubrain.springboot_starter_auth.domain.pdf.v1.PdfExtractionResultDto;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.cubrain.springboot_starter_auth.global.config.ai.AiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Real Gemini call through the production code path (language detection + page generation + JSON parsing).
 * Skipped unless GEMINI_API_KEY_IT is set, so CI and normal builds never hit the API.
 * Usage: GEMINI_API_KEY_IT=... [GEMINI_MODEL=...] ./gradlew :backend:test --tests '*GeminiLiveIntegrationTest'
 */
@Tag("live")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY_IT", matches = ".+")
class GeminiLiveIntegrationTest {

    @Test
    void generatesFlashcardsWithConfiguredModel() throws Exception {
        String model = System.getenv().getOrDefault("GEMINI_MODEL", "gemini-3.8-flash");

        AiConfig config = new AiConfig();
        ReflectionTestUtils.setField(config, "chatApiKey", System.getenv("GEMINI_API_KEY_IT"));
        ReflectionTestUtils.setField(config, "chatModelName", model);
        ReflectionTestUtils.setField(config, "chatTemperature", 1.0);

        PdfAnnotationService pdfAnnotationService = mock(PdfAnnotationService.class);
        when(pdfAnnotationService.extractAnnotations(any(Path.class), anyInt())).thenReturn(PdfExtractionResultDto.of(
                List.of(AnnotationResultDto.of(1, "Underline", "Paris",
                        "Paris is the capital and most populous city of France.", 0, 0, 0, 0)),
                "Paris is the capital and most populous city of France.", false));

        FlashcardGeneratorImpl generator = new FlashcardGeneratorImpl(config.chatLanguageModel(), new ObjectMapper(),
                pdfAnnotationService, mock(JobManager.class));

        List<FlashcardResponseDto> cards = generator.generateCardsFromPdf(Path.of("live.pdf"), UserTier.FREE_USER);

        assertFalse(cards.isEmpty());
        assertNotNull(cards.get(0).question());
        assertNotNull(cards.get(0).answer());
    }
}
