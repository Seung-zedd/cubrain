package com.cubrain.springboot_starter_auth.domain.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfAnnotationService pdfAnnotationService;

    @PostMapping("/extract-highlights")
    public ResponseEntity<List<AnnotationResultDto>> extractHighlights(@RequestParam("file") MultipartFile file)
            throws IOException {
        List<AnnotationResultDto> results = pdfAnnotationService.extractAnnotations(file);
        return ResponseEntity.ok(results);
    }
}
