package com.cubrain.springboot_starter_auth.domain.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/ingest")
@RequiredArgsConstructor
public class PdfIngestionController {

    private final PdfIngestionService pdfIngestionService;

    @PostMapping("/pdf")
    public ResponseEntity<String> ingestPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        // Validate content type and file extension
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        boolean isPdfContentType = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        boolean isPdfExtension = originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf");
        if (!isPdfContentType || !isPdfExtension) {
            return ResponseEntity.badRequest().body("Invalid file type. Only PDF files are allowed.");
        }

        try {
            pdfIngestionService.ingestPdf(file);
            return ResponseEntity.ok("PDF ingested successfully");
        } catch (Exception e) {
            log.error("Failed to ingest PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to ingest PDF. Please try again later.");
        }
    }
}
