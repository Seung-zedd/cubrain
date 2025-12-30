package com.cubrain.springboot_starter_auth.domain.card.v1.service;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.DeckResponseDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardRequestDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CardService {
        String generateCardsAsync(MultipartFile file, UserTier userTier, String ownerId);

        FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext);

        FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
                        String annotationType);

        FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
                        String annotationType, String targetLanguage);

        List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier);

        List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier, String jobId);

        DeckResponseDto saveDeck(String title, List<FlashcardResponseDto> cards, Member member);

        void updateDeckTitle(Long deckId, String newTitle);

        void updateDeckCards(Long deckId, List<FlashcardRequestDto> newCards);

        void deleteDeck(Long deckId);
}
