package com.cubrain.springboot_starter_auth.global.config.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record RootResponseDto(
        @Schema(description = "Service status", example = "UP") String status,
        @Schema(description = "Service message", example = "Service is running") String message,
        @Schema(description = "Link to API documentation (only in local)", example = "/swagger-ui.html") String docs) {

    public static RootResponseDto of(String status, String message, String docs) {
        return RootResponseDto.builder()
                .status(status)
                .message(message)
                .docs(docs)
                .build();
    }
}
