package com.cubrain.springboot_starter_auth.domain.pdf;

// DTO to hold extraction results (can be defined as inner class or separate file)
public record AnnotationResultDto(
                int pageIndex,
                String type, // Highlight or Underline
                String text, // Extracted text
                String context, // Surrounding context (paragraph, etc.)
                float x, float y, float width, float height // Coordinate information
) {
}