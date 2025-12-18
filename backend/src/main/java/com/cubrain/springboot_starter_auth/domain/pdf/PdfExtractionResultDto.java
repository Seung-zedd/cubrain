package com.cubrain.springboot_starter_auth.domain.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record PdfExtractionResultDto(
                @Schema(description = "List of extracted annotations") List<AnnotationResultDto> annotations,
                @Schema(description = "Text used for language detection") String detectionText) {
        public static PdfExtractionResultDto of(List<AnnotationResultDto> annotations, String detectionText) {
                return PdfExtractionResultDto.builder()
                                .annotations(annotations)
                                .detectionText(detectionText)
                                .build();
        }
}
