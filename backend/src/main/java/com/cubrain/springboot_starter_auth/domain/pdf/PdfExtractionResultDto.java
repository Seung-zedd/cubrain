package com.cubrain.springboot_starter_auth.domain.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PdfExtractionResultDto(
        @Schema(description = "List of extracted annotations") List<AnnotationResultDto> annotations,
        @Schema(description = "Text used for language detection") String detectionText) {
}
