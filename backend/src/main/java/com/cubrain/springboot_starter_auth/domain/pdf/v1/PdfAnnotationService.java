package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface PdfAnnotationService {
    PdfExtractionResultDto extractAnnotations(MultipartFile file, int maxPages) throws IOException;

    int getPageCount(MultipartFile file) throws IOException;
}
