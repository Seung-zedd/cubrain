package com.cubrain.springboot_starter_auth.domain.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

// DTO to hold extraction results (can be defined as inner class or separate file)
@Builder
public record AnnotationResultDto(
                @Schema(description = "Page number where annotation was found", example = "1") int pageIndex,
                @Schema(description = "Type of annotation", example = "Highlight") String type, // Highlight or
                                                                                                // Underline
                @Schema(description = "Extracted text content", example = "Important concept...") String text, // Extracted
                                                                                                               // text
                @Schema(description = "Surrounding context of the annotation", example = "This paragraph discusses...") String context, // Surrounding
                                                                                                                                        // context
                                                                                                                                        // (paragraph,
                                                                                                                                        // etc.)
                @Schema(description = "X coordinate", example = "100.0") float x,
                @Schema(description = "Y coordinate", example = "200.0") float y,
                @Schema(description = "Width of the annotation box", example = "50.0") float width,
                @Schema(description = "Height of the annotation box", example = "20.0") float height // Coordinate
                                                                                                     // information
) {
        public static AnnotationResultDto of(int pageIndex, String type, String text, String context, float x, float y,
                        float width, float height) {
                return AnnotationResultDto.builder()
                                .pageIndex(pageIndex)
                                .type(type)
                                .text(text)
                                .context(context)
                                .x(x)
                                .y(y)
                                .width(width)
                                .height(height)
                                .build();
        }
}