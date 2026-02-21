package com.cubrain.springboot_starter_auth.domain.pdf.v1;

import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.job.JobStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SSE", description = "Server-Sent Events for real-time updates")
public class SseController {

    private final JobManager jobManager;

    @Operation(summary = "Subscribe to Job Progress", description = "Subscribes to real-time progress updates for a specific job.")
    @GetMapping(value = "/subscribe/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable String jobId,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletResponse response) {
        // Critical for Railway/Nginx to prevent buffering
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        // Dynamic timeout: 5 mins for Guests, 10 mins for Authenticated
        long timeout = (jwt != null) ? 600000L : 300000L;
        SseEmitter emitter = new SseEmitter(timeout);

        JobStatus status = jobManager.getStatus(jobId);
        if (status == null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ERROR")
                        .data(Objects.requireNonNull((Object) Map.of("message", "Job not found"))));
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send error event for non-existent job: {}", jobId);
            }
            return emitter;
        }

        jobManager.addSseEmitter(jobId, emitter);

        // Send initial state
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("INIT")
                    .data(Objects.requireNonNull(Map.of(
                            "jobId", jobId,
                            "status", status,
                            "progress", jobManager.getProgress(jobId))));
            emitter.send(event);
        } catch (IOException e) {
            log.error("Failed to send INIT event for job: {}", jobId);
            jobManager.removeSseEmitter(jobId, emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
