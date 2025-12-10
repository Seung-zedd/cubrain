package com.cubrain.springboot_starter_auth.domain.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PdfAnnotationService {

    @Value("${pdf.annotation.min-text-length:2}")
    private int minTextLength;

    public List<AnnotationResultDto> extractAnnotations(MultipartFile file) throws IOException {
        List<AnnotationResultDto> results = new ArrayList<>();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                PDPage page = document.getPage(i);
                List<PDAnnotation> annotations = page.getAnnotations();

                // Prepare text stripper by area
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                // 1. Find all highlights/underlines on this page
                List<PDAnnotationTextMarkup> markups = new ArrayList<>();

                for (PDAnnotation annotation : annotations) {
                    try {
                        // Process only Highlight or Underline annotations
                        if (annotation instanceof PDAnnotationTextMarkup markup) {
                            String subType = markup.getSubtype();
                            if (PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT.equals(subType) ||
                                    PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE.equals(subType)) {

                                markups.add(markup);
                                // Register region in the extractor (unique names)
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

                // 2. Execute text extraction (coordinate-based)
                if (!markups.isEmpty()) {
                    stripper.extractRegions(page);
                }

                // 3. Save results
                for (int j = 0; j < markups.size(); j++) {
                    PDAnnotationTextMarkup markup = markups.get(j);
                    PDRectangle rect = markup.getRectangle();

                    // Get text for coordinates
                    String extractedText = stripper.getTextForRegion("annotation_" + (j + 1)).trim();
                    String contextText = stripper.getTextForRegion("context_" + (j + 1)).trim();

                    // Ignore empty or very short text (noise reduction)
                    // Configurable via pdf.annotation.min-text-length property (default: 2)
                    // Can be lowered to 1-2 to capture valid short annotations like "AI", "ML", "DNA"
                    if (!extractedText.isEmpty() && extractedText.length() > minTextLength) {
                        results.add(new AnnotationResultDto(
                                i + 1, // Page number (1-based)
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