package com.cubrain.springboot_starter_auth.domain.card.v1.dto;

import com.cubrain.springboot_starter_auth.domain.card.Flashcard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record FlashcardDto(
        @Schema(description = "Flashcard ID", example = "1") Long id,
        @Schema(description = "Question", example = "What is TCP?") String question,
        @Schema(description = "Answer", example = "TCP is a connection-oriented protocol.") String answer) {
    public static FlashcardDto from(Flashcard flashcard) {
        return FlashcardDto.builder()
                .id(flashcard.getId())
                .question(flashcard.getQuestion())
                .answer(flashcard.getAnswer())
                .build();
    }
}
