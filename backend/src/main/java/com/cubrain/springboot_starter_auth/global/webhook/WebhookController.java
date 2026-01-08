package com.cubrain.springboot_starter_auth.global.webhook;

import com.cubrain.springboot_starter_auth.global.config.lemonsqueezy.LemonSqueezyProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhook", description = "Webhook management APIs")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final LemonSqueezyProperties lemonSqueezyProperties;

    @PostMapping("/lemonsqueezy")
    @Operation(summary = "Handle Lemon Squeezy Webhook", description = "Receives and verifies webhooks from Lemon Squeezy.")
    public ResponseEntity<String> handleLemonSqueezyWebhook(
            @RequestHeader("X-Signature") String signature,
            @RequestBody String payload) {

        log.info("[Webhook] Received Lemon Squeezy webhook.");

        if (!verifySignature(payload, signature)) {
            log.warn("[Webhook] Invalid signature received.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Signature");
        }

        try {
            log.info("[Webhook] Signature verified. Processing payload...");
            webhookService.handleSubscriptionEvent(payload);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("[Webhook] Critical error during processing. Payload: {}", payload, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Processing Error");
        }
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    lemonSqueezyProperties.webhook().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hashBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // [Refactoring 1] Java 17+ HexFormat
            String computedSignature = HexFormat.of().formatHex(hashBytes);

            // [Refactoring 2] Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    computedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("[Webhook] Error verifying signature", e);
            return false;
        }
    }
}
