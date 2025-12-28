package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record JobStartResponseDto(
        @Schema(description = "The unique ID of the background job", example = "550e8400-e29b-41d4-a716-446655440000") String jobId) {
    public static JobStartResponseDto of(String jobId) {
        return JobStartResponseDto.builder()
                .jobId(jobId)
                .build();
    }
}
