package com.cubrain.springboot_starter_auth.domain.card.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record FlashcardRequestDto(
        @Schema(description = "Flashcard ID (null for new cards)", example = "1") Long id,
        @Schema(description = "Question", example = "What is TCP?") String question,
        @Schema(description = "Answer", example = "TCP is a connection-oriented protocol.") String answer,
        @Schema(description = "Page number from source PDF", example = "1") Integer page) {
    public static FlashcardRequestDto of(Long id, String question, String answer, Integer page) {
        return FlashcardRequestDto.builder()
                .id(id)
                .question(question)
                .answer(answer)
                .page(page)
                .build();
    }
}
