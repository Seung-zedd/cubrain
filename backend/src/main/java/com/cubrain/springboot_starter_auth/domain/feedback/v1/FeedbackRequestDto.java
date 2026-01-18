package com.cubrain.springboot_starter_auth.domain.feedback.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record FeedbackRequestDto(
        @Schema(description = "User's intellectual growth goals", example = "[\"Speed Reading\", \"Memory\"]") List<String> goals,

        @Schema(description = "User's pain points or 'villains'", example = "[\"Translation Villain\", \"Focus Villain\"]") List<String> painPoints,

        @Schema(description = "Short feedback message", example = "Cubrain is awesome!") String shortFeedback) {
    public static FeedbackRequestDto of(List<String> goals, List<String> painPoints, String shortFeedback) {
        return FeedbackRequestDto.builder()
                .goals(goals)
                .painPoints(painPoints)
                .shortFeedback(shortFeedback)
                .build();
    }
}
