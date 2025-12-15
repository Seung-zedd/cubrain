package com.cubrain.springboot_starter_auth.domain.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.text.PDFTextStripper;
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

    @Value("${pdf.annotation.min-text-length:1}")
    private int minTextLength;

    public PdfExtractionResultDto extractAnnotations(MultipartFile file) throws IOException {
        log.debug("Processing PDF file: {}", file.getOriginalFilename());
        List<AnnotationResultDto> results = new ArrayList<>();
        String detectionText = "";

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            int pageCount = document.getNumberOfPages();

            // Extract text from the first available page with sufficient content for
            // language detection
            // Scan up to the first 5 pages (or fewer if document is shorter)
            int maxScanPages = Math.min(pageCount, 5);

            for (int p = 1; p <= maxScanPages; p++) {
                PDFTextStripper textStripper = new PDFTextStripper();
                textStripper.setStartPage(p);
                textStripper.setEndPage(p);
                String pageText = textStripper.getText(document).trim();

                // If page has sufficient text (e.g. > 50 chars), use it and stop scanning
                if (pageText.length() > 50) {
                    detectionText = pageText;
                    // Limit text length to avoid excessive token usage
                    if (detectionText.length() > 2000) {
                        detectionText = detectionText.substring(0, 2000);
                    }
                    log.debug("Found sufficient text for language detection on page {}", p);
                    break;
                }
            }

            for (int i = 0; i < pageCount; i++) {
                PDPage page = document.getPage(i);
                List<PDAnnotation> annotations = page.getAnnotations();

                // Prepare text stripper by area
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                // Get Page Height for Coordinate Conversion (PDF Bottom-Left -> AWT Top-Left)
                float pageHeight = page.getCropBox().getHeight();

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

                                float x = rect.getLowerLeftX();
                                float y = rect.getLowerLeftY();
                                float w = rect.getWidth();
                                float h = rect.getHeight();

                                // General expansion for all types to handle sloppy boundaries
                                float expansion = 2.0f;
                                x -= expansion;
                                y -= expansion;
                                w += (expansion * 2);
                                h += (expansion * 2);

                                // Specific logic for Underlines
                                if (PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE.equals(subType)) {
                                    h += 15.0f;
                                    y -= 2.0f;
                                    h += 2.0f;
                                }

                                // We want the distance from the top of the page to the top edge of the box.
                                float awtY = pageHeight - (y + h);

                                stripper.addRegion("annotation_" + markups.size(),
                                        new java.awt.geom.Rectangle2D.Float(x, awtY, w, h));

                                // Context extraction region (expand vertically, full width)
                                float padding = 150f;
                                PDRectangle pageRect = page.getCropBox();
                                float cX = pageRect.getLowerLeftX();
                                float cW = pageRect.getWidth();

                                // Context Y (Bottom-Left in PDF)
                                float pdfContextBottomY = rect.getLowerLeftY() - padding;
                                float contextHeight = rect.getHeight() + (padding * 2);

                                // Convert Context Y to Top-Left
                                float awtContextY = pageHeight - (pdfContextBottomY + contextHeight);

                                stripper.addRegion("context_" + markups.size(),
                                        new java.awt.geom.Rectangle2D.Float(cX, awtContextY, cW, contextHeight));
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
                    } else {
                        log.info("⚠️ Skipped [{}]: Page {}, Text: \"{}\" (Length: {})", markup.getSubtype(), i + 1,
                                extractedText, extractedText.length());
                    }
                }
            }
        }

        // Sort by Reading Order
        results.sort(java.util.Comparator
                .comparingInt(AnnotationResultDto::pageIndex)
                .thenComparing((a, b) -> Float.compare(b.y(), a.y())) // Descending Y
                .thenComparingDouble(AnnotationResultDto::x));

        if (results.isEmpty()) {
            log.warn("User uploaded a clean PDF. No flashcards generated.");
        }

        return new PdfExtractionResultDto(results, detectionText);
    }
}