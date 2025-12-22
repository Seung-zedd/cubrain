package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import com.cubrain.springboot_starter_auth.domain.job.JobStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record JobStatusResponseDto(
        @Schema(description = "Current status of the job", example = "PROCESSING") JobStatus status,
        @Schema(description = "Progress percentage", example = "45") int progress,
        @Schema(description = "Results of the job if completed") Object results) {
    public static JobStatusResponseDto of(JobStatus status, int progress, Object results) {
        return JobStatusResponseDto.builder()
                .status(status)
                .progress(progress)
                .results(results)
                .build();
    }
}
