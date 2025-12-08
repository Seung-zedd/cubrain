package com.cubrain.springboot_starter_auth.domain.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PdfAnnotationService {

    public List<AnnotationResultDto> extractAnnotations(MultipartFile file) throws IOException {
        List<AnnotationResultDto> results = new ArrayList<>();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                PDPage page = document.getPage(i);
                List<PDAnnotation> annotations = page.getAnnotations();

                // 영역별 텍스트 추출기 준비
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                // 1. 해당 페이지의 모든 하이라이트/밑줄 찾기
                List<PDAnnotationTextMarkup> markups = new ArrayList<>();

                for (PDAnnotation annotation : annotations) {
                    try {
                        // 하이라이트(Highlight) 또는 밑줄(Underline)인 경우만 처리
                        if (annotation instanceof PDAnnotationTextMarkup markup) {
                            String subType = markup.getSubtype();
                            if (PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT.equals(subType) ||
                                    PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE.equals(subType)) {

                                markups.add(markup);
                                // 추출기에 영역 등록 (이름은 유니크하게)
                                PDRectangle rect = markup.getRectangle();
                                stripper.addRegion("annotation_" + markups.size(), new java.awt.geom.Rectangle2D.Float(
                                        rect.getLowerLeftX(),
                                        rect.getLowerLeftY(),
                                        rect.getWidth(),
                                        rect.getHeight()));

                                // Context extraction region (expand vertically, full width)
                                float padding = 150f;
                                PDRectangle pageRect = page.getCropBox();
                                float cX = pageRect.getLowerLeftX();
                                float cY = rect.getLowerLeftY() - padding;
                                float cW = pageRect.getWidth();
                                float cH = rect.getHeight() + (padding * 2);

                                stripper.addRegion("context_" + markups.size(),
                                        new java.awt.geom.Rectangle2D.Float(cX, cY, cW, cH));
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Skipping malformed annotation on page {}", i + 1, e);
                    }
                }

                // 2. 텍스트 추출 실행 (좌표 기반)
                if (!markups.isEmpty()) {
                    stripper.extractRegions(page);
                }

                // 3. 결과 저장
                for (int j = 0; j < markups.size(); j++) {
                    PDAnnotationTextMarkup markup = markups.get(j);
                    PDRectangle rect = markup.getRectangle();

                    // 좌표에 해당하는 텍스트 가져오기
                    String extractedText = stripper.getTextForRegion("annotation_" + (j + 1)).trim();
                    String contextText = stripper.getTextForRegion("context_" + (j + 1)).trim();

                    // 빈 텍스트나 너무 짧은 건 무시 (노이즈 제거)
                    if (!extractedText.isEmpty() && extractedText.length() > 3) {
                        results.add(new AnnotationResultDto(
                                i + 1, // 페이지 번호 (1부터 시작)
                                markup.getSubtype(),
                                extractedText,
                                contextText,
                                rect.getLowerLeftX(),
                                rect.getLowerLeftY(),
                                rect.getWidth(),
                                rect.getHeight()));

                        log.info("🔍 Found [{}]: Page {}, Text: \"{}\"", markup.getSubtype(), i + 1, extractedText);
                    }
                }
            }
        }

        // Sort by Reading Order: Page (Asc) -> Y (Desc, Top-to-Bottom) -> X (Asc,
        // Left-to-Right)
        results.sort(java.util.Comparator
                .comparingInt(AnnotationResultDto::pageIndex)
                .thenComparing((a, b) -> Float.compare(b.y(), a.y())) // Descending Y (Top is higher Y in PDF)
                .thenComparingDouble(AnnotationResultDto::x));

        if (results.isEmpty()) {
            log.warn("User uploaded a clean PDF. No flashcards generated.");
        }

        return results;
    }
}