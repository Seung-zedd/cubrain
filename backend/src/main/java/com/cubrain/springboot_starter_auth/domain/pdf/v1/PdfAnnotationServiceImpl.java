package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class PdfAnnotationServiceImpl implements PdfAnnotationService {

    @Value("${pdf.annotation.min-text-length:1}")
    private int minTextLength;

    @Override
    public PdfExtractionResultDto extractAnnotations(Path filePath, int maxPages) throws IOException {
        List<AnnotationResultDto> results = new ArrayList<>();
        String detectionText = "";

        try (PDDocument document = PDDocument.load(filePath.toFile())) {
            int pageCount = document.getNumberOfPages();

            int maxScanPages = Math.min(pageCount, 5);

            for (int p = 1; p <= maxScanPages; p++) {
                PDFTextStripper textStripper = new PDFTextStripper();
                textStripper.setStartPage(p);
                textStripper.setEndPage(p);
                String pageText = textStripper.getText(document).trim();

                if (pageText.length() > 50) {
                    detectionText = pageText;
                    if (detectionText.length() > 2000) {
                        detectionText = detectionText.substring(0, 2000);
                    }
                    log.debug("Found sufficient text for language detection on page {}", p);
                    break;
                }
            }

            PDFRenderer pdfRenderer = null;
            int pagesToProcess = Math.min(pageCount, maxPages);
            for (int i = 0; i < pagesToProcess; i++) {
                PDPage page = document.getPage(i);
                List<PDAnnotation> annotations = page.getAnnotations();

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                float pageHeight = page.getCropBox().getHeight();

                List<PDAnnotation> processedAnnotations = new ArrayList<>();

                for (PDAnnotation annotation : annotations) {
                    try {
                        String subType = annotation.getSubtype();
                        boolean isHighlightOrUnderline = PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT.equals(subType) ||
                                PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE.equals(subType);
                        boolean isInk = "Ink".equals(subType);
                        boolean isFreeText = "FreeText".equals(subType);

                        if (isHighlightOrUnderline || isInk || isFreeText) {
                            processedAnnotations.add(annotation);
                            PDRectangle rect = annotation.getRectangle();
                            if (rect == null) {
                                continue;
                            }

                            float x = rect.getLowerLeftX();
                            float y = rect.getLowerLeftY();
                            float w = rect.getWidth();
                            float h = rect.getHeight();

                            if (isHighlightOrUnderline) {
                                java.awt.geom.Rectangle2D.Float annotRegion = PdfCoordinateUtils.calculateAnnotationRegion(
                                        x, y, w, h, pageHeight, subType);
                                stripper.addRegion("annotation_" + processedAnnotations.size(), annotRegion);
                            }

                            // Context extraction using coordinate padding
                            float padding = 150f;
                            PDRectangle pageRect = page.getCropBox();
                            java.awt.geom.Rectangle2D.Float contextRegion = PdfCoordinateUtils.calculateContextRegion(
                                    rect, pageRect, pageHeight, padding);
                            stripper.addRegion("context_" + processedAnnotations.size(), contextRegion);
                        }
                    } catch (Exception e) {
                        log.warn("Skipping malformed annotation on page {}", i + 1, e);
                    }
                }

                if (!processedAnnotations.isEmpty()) {
                    stripper.extractRegions(page);
                }

                // Render page to image on-demand if there are Ink annotations
                boolean hasInk = processedAnnotations.stream().anyMatch(a -> "Ink".equals(a.getSubtype()));
                BufferedImage pageImage = null;
                if (hasInk) {
                    if (pdfRenderer == null) {
                        pdfRenderer = new PDFRenderer(document);
                    }
                    try {
                        // Render at 2.0f scale (144 DPI) for clarity in vision analysis
                        pageImage = pdfRenderer.renderImage(i, 2.0f);
                    } catch (Exception e) {
                        log.error("Failed to render page {} to image for Ink annotation cropping", i + 1, e);
                    }
                }

                for (int j = 0; j < processedAnnotations.size(); j++) {
                    PDAnnotation annotation = processedAnnotations.get(j);
                    String subType = annotation.getSubtype();
                    PDRectangle rect = annotation.getRectangle();
                    if (rect == null) {
                        continue;
                    }

                    boolean isInk = "Ink".equals(subType);
                    boolean isFreeText = "FreeText".equals(subType);
                    String extractedText = "";
                    if (isFreeText) {
                        extractedText = annotation.getContents();
                        if (extractedText == null) {
                            extractedText = "";
                        }
                        extractedText = extractedText.trim();
                    } else if (!isInk) {
                        extractedText = stripper.getTextForRegion("annotation_" + (j + 1)).trim();
                    } else {
                        extractedText = "[Ink Drawing]";
                    }

                    String contextText = stripper.getTextForRegion("context_" + (j + 1)).trim();
                    String base64Image = null;

                    if (isInk && pageImage != null) {
                        try {
                            float scale = 2.0f;
                            float cropPadding = 5.0f;
                            int imgWidth = pageImage.getWidth();
                            int imgHeight = pageImage.getHeight();

                            PdfCoordinateUtils.PixelBounds pb = PdfCoordinateUtils.calculatePixelBounds(
                                    rect, pageHeight, scale, cropPadding, imgWidth, imgHeight);

                            BufferedImage cropped = pageImage.getSubimage(pb.x(), pb.y(), pb.width(), pb.height());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            javax.imageio.ImageIO.write(cropped, "png", baos);
                            base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                        } catch (Exception e) {
                            log.error("Failed to crop image for Ink annotation on page {}", i + 1, e);
                        }
                    }

                    if (isInk || isFreeText || (!extractedText.isEmpty() && extractedText.length() > minTextLength)) {
                        results.add(AnnotationResultDto.of(
                                i + 1,
                                subType,
                                extractedText,
                                contextText,
                                rect.getLowerLeftX(),
                                rect.getLowerLeftY(),
                                rect.getWidth(),
                                rect.getHeight(),
                                base64Image));

                        log.info("🔍 Found [{}]: Page {}, Text: \"{}\", HasImage: {}",
                                subType, i + 1, extractedText, base64Image != null);
                    } else {
                        log.info("⚠️ Skipped [{}]: Page {}, Text: \"{}\" (Length: {})", subType, i + 1,
                                extractedText, extractedText.length());
                    }
                }
            }

            results.sort(Comparator
                    .comparingInt(AnnotationResultDto::pageIndex)
                    .thenComparing(a -> "Ink".equals(a.type()))
                    .thenComparing((a, b) -> Float.compare(b.y(), a.y()))
                    .thenComparingDouble(AnnotationResultDto::x));

            if (results.isEmpty()) {
                log.warn("User uploaded a clean PDF. No flashcards generated.");
            }
            boolean isLimited = pageCount > maxPages;
            return PdfExtractionResultDto.of(results, detectionText, isLimited);
        }
    }

    @Override
    public int getPageCount(Path filePath) throws IOException {
        try (PDDocument document = PDDocument.load(filePath.toFile())) {
            return document.getNumberOfPages();
        }
    }
}
