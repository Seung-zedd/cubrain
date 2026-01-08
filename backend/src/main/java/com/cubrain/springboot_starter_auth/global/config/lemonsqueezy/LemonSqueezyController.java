package com.cubrain.springboot_starter_auth.global.config.lemonsqueezy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/lemonsqueezy")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment and Subscription management APIs")
public class LemonSqueezyController {

    private final LemonSqueezyProperties lemonSqueezyProperties;

    @GetMapping("/config")
    @Operation(summary = "Get Lemon Squeezy Configuration", description = "Returns the environment-specific Lemon Squeezy product and variant IDs. Requires authentication.")
    public ResponseEntity<LemonSqueezyConfigDto> getLemonSqueezyConfig() {
        log.info("💳 [LemonSqueezy] Fetching configuration for authenticated user.");
        return new ResponseEntity<>(LemonSqueezyConfigDto.from(lemonSqueezyProperties), OK);
    }
}
