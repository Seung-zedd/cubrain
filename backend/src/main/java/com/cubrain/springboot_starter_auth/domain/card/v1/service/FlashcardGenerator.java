package com.cubrain.springboot_starter_auth.domain.card.v1.service;

import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardResponseDto;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import java.util.List;

public interface FlashcardGenerator {
        FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext);

        FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
                        String annotationType);

        FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
                        String annotationType, String targetLanguage);

        List<FlashcardResponseDto> generateCardsFromPdf(byte[] fileData, UserTier userTier);

        List<FlashcardResponseDto> generateCardsFromPdf(byte[] fileData, UserTier userTier, String jobId);
}
