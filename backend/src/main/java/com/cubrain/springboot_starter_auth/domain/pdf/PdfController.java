package com.cubrain.springboot_starter_auth.domain.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cubrain.springboot_starter_auth.domain.card.CardService;
import com.cubrain.springboot_starter_auth.domain.job.JobManager;
import com.cubrain.springboot_starter_auth.domain.job.JobStatus;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.COMPLETED;
import static com.cubrain.springboot_starter_auth.domain.user.UserTier.GUEST;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfAnnotationService pdfAnnotationService;
    private final CardService cardService;
    private final JobManager jobManager;

    @PostMapping("/extract-highlights")
    public ResponseEntity<List<AnnotationResultDto>> extractHighlights(@RequestParam("file") MultipartFile file)
            throws IOException {
        List<AnnotationResultDto> results = pdfAnnotationService.extractAnnotations(file);
        return ok(results);
    }

    @PostMapping("/generate-cards")
    public ResponseEntity<Map<String, String>> generateCardsAsync(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tier", defaultValue = "GUEST") UserTier tier) {
        String jobId = cardService.generateCardsAsync(file, tier);
        return ok(Map.of("jobId", jobId));
    }

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
