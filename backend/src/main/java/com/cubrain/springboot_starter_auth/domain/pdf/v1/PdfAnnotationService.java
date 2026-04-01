package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import java.io.IOException;

public interface PdfAnnotationService {
    PdfExtractionResultDto extractAnnotations(byte[] fileData, int maxPages) throws IOException;

    int getPageCount(byte[] fileData) throws IOException;
}
