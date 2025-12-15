package com.cubrain.springboot_starter_auth.domain.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;

import com.cubrain.springboot_starter_auth.domain.card.CardService;
import com.cubrain.springboot_starter_auth.domain.job.JobManager;
import com.cubrain.springboot_starter_auth.domain.job.JobStatus;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.COMPLETED;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfAnnotationService pdfAnnotationService;
    private final CardService cardService;
    private final JobManager jobManager;

    @Operation(summary = "Extract Highlights", description = "Extracts highlights and underlines from a PDF file.")
    @PostMapping("/extract-highlights")
    public ResponseEntity<List<AnnotationResultDto>> extractHighlights(@RequestParam MultipartFile file)
            throws IOException {
        PdfExtractionResultDto result = pdfAnnotationService.extractAnnotations(file);
        List<AnnotationResultDto> results = result.annotations();
        return ok(results);
    }

    @Operation(summary = "Generate Cards Async", description = "Starts an asynchronous job to generate flashcards from a PDF.")
    @PostMapping("/generate-cards")
    public ResponseEntity<Map<String, String>> generateCardsAsync(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "GUEST") UserTier userTier) {
        String jobId = cardService.generateCardsAsync(file, userTier);
        return ok(Map.of("jobId", jobId));
    }

    @Operation(summary = "Get Job Status", description = "Retrieves the status and results of a background job.")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(
            @PathVariable String jobId) {
        JobStatus status = jobManager.getStatus(jobId);
        if (status == null) {
            return notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("progress", jobManager.getProgress(jobId));

        if (status == COMPLETED) {
            response.put("results", jobManager.getResults(jobId));
        }

        return ok(response);
    }
}
