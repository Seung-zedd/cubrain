package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.awt.geom.Rectangle2D;

/**
 * Utility class for PDF coordinate transformations, bounding box expansion, 
 * and pixel boundary calculations.
 */
public class PdfCoordinateUtils {

    /**
     * Calculates the target text/drawing extraction region bounds in AWT coordinate system (origin top-left).
     */
    public static Rectangle2D.Float calculateAnnotationRegion(
            float x, float y, float w, float h, float pageHeight, String subType) {
        
        float expansion = 2.0f;
        float rx = x - expansion;
        float ry = y - expansion;
        float rw = w + (expansion * 2);
        float rh = h + (expansion * 2);

        if ("Underline".equals(subType)) {
            rh += 15.0f;
            ry -= 2.0f;
            rh += 2.0f;
        }

        float awtY = pageHeight - (ry + rh);
        return new Rectangle2D.Float(rx, awtY, rw, rh);
    }

    /**
     * Calculates the surrounding context text extraction region bounds using coordinate padding.
     */
    public static Rectangle2D.Float calculateContextRegion(
            PDRectangle rect, PDRectangle pageRect, float pageHeight, float padding) {
        
        float cX = pageRect.getLowerLeftX();
        float cW = pageRect.getWidth();

        float pdfContextBottomY = rect.getLowerLeftY() - padding;
        float contextHeight = rect.getHeight() + (padding * 2);

        float awtContextY = pageHeight - (pdfContextBottomY + contextHeight);
        return new Rectangle2D.Float(cX, awtContextY, cW, contextHeight);
    }

    /**
     * Calculates scaled pixel bounds for image cropping with safe boundary guards.
     */
    public static PixelBounds calculatePixelBounds(
            PDRectangle rect, float pageHeight, float scale, float cropPadding, int imgWidth, int imgHeight) {
        
        float x = rect.getLowerLeftX() - cropPadding;
        float y = rect.getLowerLeftY() - cropPadding;
        float w = rect.getWidth() + (cropPadding * 2);
        float h = rect.getHeight() + (cropPadding * 2);

        float awtY = pageHeight - (y + h);

        int pixelX = Math.round(x * scale);
        int pixelY = Math.round(awtY * scale);
        int pixelW = Math.round(w * scale);
        int pixelH = Math.round(h * scale);

        int cropX = Math.max(0, Math.min(pixelX, imgWidth - 1));
        int cropY = Math.max(0, Math.min(pixelY, imgHeight - 1));
        int cropW = Math.max(1, Math.min(pixelW, imgWidth - cropX));
        int cropH = Math.max(1, Math.min(pixelH, imgHeight - cropY));

        return new PixelBounds(cropX, cropY, cropW, cropH);
    }

    /**
     * Represents the calculated AWT pixel boundaries for subimage cropping.
     */
    public record PixelBounds(int x, int y, int width, int height) {}
}
