package com.cubrain.springboot_starter_auth.domain.waitlist;

import com.cubrain.springboot_starter_auth.domain.waitlist.dto.WaitlistRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow Svelte to call this
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    public ResponseEntity<String> joinWaitlist(@RequestBody WaitlistRequestDto requestDto) {
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
