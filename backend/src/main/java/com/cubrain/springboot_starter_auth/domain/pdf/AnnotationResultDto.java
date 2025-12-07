package com.cubrain.springboot_starter_auth.domain.pdf;

// 추출 결과를 담을 DTO (내부 클래스로 정의하거나 따로 파일로 빼셔도 됩니다)
public record AnnotationResultDto(
                int pageIndex,
                String type, // Highlight or Underline
                String text, // 추출된 텍스트
                String context, // 주변 문맥 (문단 등)
                float x, float y, float width, float height // 좌표 정보
) {
}