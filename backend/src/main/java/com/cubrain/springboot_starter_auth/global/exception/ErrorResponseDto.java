package com.cubrain.springboot_starter_auth.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponseDto(
        @Schema(description = "Timestamp of the error", example = "2023-10-27T10:00:00") LocalDateTime timestamp,
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "HTTP error reason", example = "Not Found") String error,
        @Schema(description = "Error message", example = "Resource not found.") String message,
        @Schema(description = "Detailed error information (only in local)", example = "Entity with ID 1 not found") String details) {

    public static ErrorResponseDto of(int status, String error, String message, String details) {
        return ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .details(details)
                .build();
    }
}
