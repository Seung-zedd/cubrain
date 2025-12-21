package com.cubrain.springboot_starter_auth.domain.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record FlashcardResponseDto(
                @Schema(description = "Generated question", example = "What is TCP?") String question,

                @Schema(description = "Generated answer", example = "TCP is a connection-oriented protocol.") String answer) {
        public static FlashcardResponseDto of(String question, String answer) {
                return FlashcardResponseDto.builder()
                                .question(question)
                                .answer(answer)
                                .build();
        }
}