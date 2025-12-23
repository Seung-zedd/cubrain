package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.job.JobStatus;
import com.cubrain.springboot_starter_auth.domain.card.v1.CardService;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.global.usage.UsageLimitService;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Map;

import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.COMPLETED;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
@Slf4j
public class PdfIngestionController {

    private final CardService cardService;
    private final JobManager jobManager;
    private final UsageLimitService usageLimitService;
    private final MemberRepository memberRepository;

    @Operation(summary = "Generate Flashcards from PDF", description = "Uploads and processes a PDF file asynchronously to generate flashcards.")
    @PostMapping("/generate-cards")
    public ResponseEntity<?> generateCards(
            @RequestParam("file") MultipartFile file,
            Principal principal,
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
        String ownerId = request.getRemoteAddr();

        if (principal != null) {
            Optional<Member> memberOpt = memberRepository.findByEmail(principal.getName());
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                userTier = member.getTier();
                ownerId = member.getEmail();
                try {
                    usageLimitService.checkAndIncrement(member);
                } catch (IllegalStateException e) {
                    return status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));
                }
            }
        } else {
            try {
                usageLimitService.checkAndIncrementGuest(ownerId);
            } catch (IllegalStateException e) {
                return status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));
            }
        }

        String jobId = cardService.generateCardsAsync(file, userTier, ownerId);
        return ok(JobStartResponseDto.of(jobId));
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

        return ok(JobStatusResponseDto.of(status, progress, results));
    }
}
