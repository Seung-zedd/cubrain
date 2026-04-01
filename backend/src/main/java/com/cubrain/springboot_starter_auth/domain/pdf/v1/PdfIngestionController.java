package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.job.JobStatus;
import com.cubrain.springboot_starter_auth.domain.card.v1.service.CardService;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.global.usage.UsageLimitService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.Optional;
import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.COMPLETED;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PDF Ingestion", description = "PDF processing and flashcard generation APIs")
public class PdfIngestionController {

    private final CardService cardService;
    private final JobManager jobManager;
    private final UsageLimitService usageLimitService;
    private final MemberRepository memberRepository;
    private final PdfAnnotationService pdfAnnotationService;

    @Operation(summary = "Generate Flashcards from PDF", description = "Uploads and processes a PDF file asynchronously to generate flashcards.")
    @PostMapping("/generate-cards")
    public ResponseEntity<?> generateCards(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        if (file.isEmpty()) {
            return badRequest().body(Map.of("error", "File is empty"));
        }

        // Validate content type and file extension
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        boolean isPdfContentType = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        boolean isPdfExtension = originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf");

        if (!isPdfContentType || !isPdfExtension) {
            return badRequest().body(Map.of("error", "Invalid file type. Only PDF files are allowed."));
        }

        UserTier userTier = UserTier.GUEST;
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        String email = jwt != null ? jwt.getClaimAsString("email") : null;
        String ownerId = email != null ? email.toLowerCase() : clientIp;

        log.info("[UsageLimit] Request from jwt: {}, ownerId: {}",
                email != null ? email : "null", ownerId);

        Member member = null;
        if (jwt != null && email != null) {
            Optional<Member> memberOpt = memberRepository.findByEmail(email.toLowerCase());
            if (memberOpt.isPresent()) {
                member = memberOpt.get();
                userTier = member.getTier();
            }
        }

        byte[] fileData;
        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read upload file bytes", e);
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to read uploaded file"));
        }

        // 1. Validate Page Count (Log for analytics)
        try {
            int pageCount = pdfAnnotationService.getPageCount(fileData);
            log.info("Processing PDF: {} ({} pages) for user: {}", originalFilename, pageCount, ownerId);
        } catch (Exception e) {
            log.warn("Could not read PDF page count for logging", e);
        }

        // 2. Check and Increment Usage (Cost Defense)
        try {
            if (jwt != null && email != null) {
                log.info("[UsageLimit] Checking limit for AUTH_USER: {}", ownerId);
                usageLimitService.checkAndIncrement(ownerId);
            } else {
                log.info("[UsageLimit] Checking limit for GUEST: {}", ownerId);
                usageLimitService.checkAndIncrementGuest(ownerId);
            }
        } catch (IllegalStateException e) {
            log.warn("[UsageLimit] Limit reached for {}: {}", ownerId, e.getMessage());
            return status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));
        }

        String jobId = cardService.generateCardsAsync(fileData, originalFilename, userTier, ownerId);
        return ok(JobStartResponseDto.of(jobId));
    }

    @Operation(summary = "Get Recent Job", description = "Retrieves the most recent job for the current user or guest.")
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentJob(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        String email = jwt != null ? jwt.getClaimAsString("email") : null;
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        String ownerId = email != null ? email.toLowerCase() : clientIp;
        log.info("[Job] Fetching recent job for owner: {}", ownerId);

        // 1. Try to find the last non-dismissed job for the primary owner
        Optional<String> jobIdOpt = jobManager.findAllJobsByOwner(ownerId).stream()
                .filter(jobId -> !isDismissed(jobId))
                .findFirst();

        // 2. If authenticated but no job found for email, check IP
        if (jobIdOpt.isEmpty() && email != null) {
            log.info("[Job] No email job found, checking IP: {}", clientIp);
            jobIdOpt = jobManager.findAllJobsByOwner(clientIp).stream()
                    .filter(jobId -> !isDismissed(jobId))
                    .findFirst();
            if (jobIdOpt.isPresent()) {
                String jobId = jobIdOpt.get();
                log.info("[Job] Claiming guest job {} for user {}", jobId, email);
                jobManager.changeOwner(jobId, email.toLowerCase());
            }
        }

        return jobIdOpt
                .map(jobId -> {
                    JobStatus status = jobManager.getStatus(jobId);
                    int progress = jobManager.getProgress(jobId);
                    Object results = status == COMPLETED ? jobManager.getResults(jobId) : null;
                    Map<String, Object> metadata = jobManager.getMetadata(jobId);
                    log.info("[Job] Found recent job: {} (Status: {})", jobId, status);
                    return ok(JobStatusResponseDto.of(status, progress, results, metadata));
                })
                .orElseGet(() -> {
                    log.info("[Job] No recent non-dismissed job found for {}", ownerId);
                    return notFound().build();
                });
    }

    private boolean isDismissed(String jobId) {
        Map<String, Object> metadata = jobManager.getMetadata(jobId);
        return Boolean.TRUE.equals(metadata.get("dismissed"));
    }

    @Operation(summary = "Dismiss Job", description = "Marks a job as dismissed so it no longer appears as the recent job.")
    @PostMapping("/jobs/{jobId}/dismiss")
    public ResponseEntity<?> dismissJob(@PathVariable String jobId) {
        if (jobManager.getStatus(jobId) == null) {
            return notFound().build();
        }
        log.info("[Job] Dismissing job: {}", jobId);
        jobManager.updateMetadata(jobId, "dismissed", true);
        return ok().build();
    }

    @Operation(summary = "Dismiss All Jobs", description = "Marks all jobs for the current user as dismissed.")
    @PostMapping("/jobs/dismiss-all")
    public ResponseEntity<?> dismissAllJobs(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        String email = jwt != null ? jwt.getClaimAsString("email") : null;
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        String ownerId = email != null ? email.toLowerCase() : clientIp;

        log.info("[Job] Dismissing all jobs for owner: {}", ownerId);
        jobManager.findAllJobsByOwner(ownerId).forEach(jobId -> jobManager.updateMetadata(jobId, "dismissed", true));
        return ok().build();
    }

    @Operation(summary = "Get Job Status", description = "Retrieves the status and results of a background job.")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobStatusResponseDto> getJobStatus(@PathVariable String jobId) {
        JobStatus status = jobManager.getStatus(jobId);
        if (status == null) {
            return notFound().build();
        }

        int progress = jobManager.getProgress(jobId);
        Object results = null;

        if (status == COMPLETED) {
            results = jobManager.getResults(jobId);
        }

        Map<String, Object> metadata = jobManager.getMetadata(jobId);
        return ok(JobStatusResponseDto.of(status, progress, results, metadata));
    }
}
