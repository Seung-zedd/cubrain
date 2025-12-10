package com.cubrain.springboot_starter_auth.domain.pdf;

import com.cubrain.springboot_starter_auth.domain.job.JobManager;
import com.cubrain.springboot_starter_auth.domain.job.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.COMPLETED;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/ingest")
@RequiredArgsConstructor
@Slf4j
public class PdfIngestionController {

    private final PdfIngestionService pdfIngestionService;
    private final JobManager jobManager;

    @PostMapping("/pdf")
    public ResponseEntity<Map<String, String>> ingestPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        // Validate content type and file extension
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        boolean isPdfContentType = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        boolean isPdfExtension = originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf");
        if (!isPdfContentType || !isPdfExtension) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type. Only PDF files are allowed."));
        }

        String jobId = pdfIngestionService.ingestPdfAsync(file);
        return ok(Map.of("jobId", jobId, "message", "PDF ingestion started"));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        JobStatus status = jobManager.getStatus(jobId);
        if (status == null) {
            return notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("progress", jobManager.getProgress(jobId));

        if (status == COMPLETED) {
            response.put("result", jobManager.getResults(jobId));
        }

        return ok(response);
    }
}
