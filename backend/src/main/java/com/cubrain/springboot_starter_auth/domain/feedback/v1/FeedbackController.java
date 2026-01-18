package com.cubrain.springboot_starter_auth.domain.feedback.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feedback", description = "User feedback and survey APIs")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Submit user feedback", description = "Submits survey responses and sends an email to support")
    public ResponseEntity<Void> submitFeedback(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody FeedbackRequestDto dto) {
        if (jwt == null) {
            log.warn("🚨 Unauthorized feedback submission attempt");
            return ResponseEntity.status(401).build();
        }

        String email = jwt.getClaimAsString("email");
        String sub = jwt.getSubject();
        String senderInfo = String.format("%s (Sub: %s)", email, sub);

        log.info("📩 Receiving feedback from: {}", senderInfo);
        feedbackService.sendFeedbackEmail(dto, senderInfo);

        return ResponseEntity.ok().build();
    }
}
