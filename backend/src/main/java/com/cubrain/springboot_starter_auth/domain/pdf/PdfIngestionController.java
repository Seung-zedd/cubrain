package com.cubrain.springboot_starter_auth.domain.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

        try {
            pdfIngestionService.ingestPdf(file);
            return ResponseEntity.ok("PDF ingested successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to ingest PDF: " + e.getMessage());
        }
    }
}
