package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record PdfExtractionResultDto(
                @Schema(description = "List of extracted annotations") List<AnnotationResultDto> annotations,
                @Schema(description = "Text used for language detection") String detectionText,
                @Schema(description = "Whether the extraction was limited by page count") boolean isLimited) {
        public static PdfExtractionResultDto of(List<AnnotationResultDto> annotations, String detectionText,
                        boolean isLimited) {
                return PdfExtractionResultDto.builder()
                                .annotations(annotations)
                                .detectionText(detectionText)
                                .isLimited(isLimited)
                                .build();
        }
}
