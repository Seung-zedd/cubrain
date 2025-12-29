package com.cubrain.springboot_starter_auth.domain.card.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DeckCreateRequestDto(
                @Schema(description = "Deck title", example = "Introduction to AI") String title,
                @Schema(description = "List of flashcards to save") List<FlashcardResponseDto> cards) {
}
