package com.cubrain.springboot_starter_auth.domain.card.v1.dto;

import com.cubrain.springboot_starter_auth.domain.card.Deck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.Instant;

@Builder(access = AccessLevel.PRIVATE)
public record DeckResponseDto(
        @Schema(description = "Deck ID", example = "1") Long id,
        @Schema(description = "Deck title", example = "Introduction to AI") String title,
        @Schema(description = "Number of cards in the deck", example = "15") long cardCount,
        @Schema(description = "Last studied date", example = "2023-10-27T10:00:00Z") Instant lastStudiedAt,
        @Schema(description = "Study progress percentage", example = "50") Integer studyProgress,
        @Schema(description = "Created date", example = "2023-10-27T10:00:00Z") Instant createdAt) {
    public static DeckResponseDto from(Deck deck, long cardCount) {
        return DeckResponseDto.builder()
                .id(deck.getId())
                .title(deck.getTitle())
                .cardCount(cardCount)
                .lastStudiedAt(deck.getLastStudiedAt())
                .studyProgress(deck.getStudyProgress())
                .createdAt(deck.getCreatedAt())
                .build();
    }
}
