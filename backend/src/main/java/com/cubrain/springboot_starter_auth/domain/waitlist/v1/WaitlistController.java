package com.cubrain.springboot_starter_auth.domain.waitlist.v1;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/waitlist")
@RequiredArgsConstructor
@Slf4j
public class WaitlistController {

    private final WaitlistService waitlistService;

    @Operation(summary = "Join Waitlist", description = "Adds a user to the waitlist.")
    @PostMapping
    public ResponseEntity<String> joinWaitlist(@RequestBody @Valid WaitlistRequestDto requestDto) {
        log.info("📩 Waitlist join request received for email: {}", requestDto.email());
        try {
            String message = waitlistService.joinWaitlist(requestDto);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}
