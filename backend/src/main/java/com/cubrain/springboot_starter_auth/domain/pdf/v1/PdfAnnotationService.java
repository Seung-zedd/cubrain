package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import java.io.IOException;

import java.nio.file.Path;

public interface PdfAnnotationService {
    PdfExtractionResultDto extractAnnotations(Path filePath, int maxPages) throws IOException;

    int getPageCount(Path filePath) throws IOException;
}
