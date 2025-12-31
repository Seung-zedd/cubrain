package com.cubrain.springboot_starter_auth.global.webhook;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class WebhookController {

    @Value("${lemonsqueezy.webhook.secret}")
    private String webhookSecret;

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
            log.info("[Webhook] Signature verified. Payload: {}", payload);

            // TODO: Process the webhook payload based on event type (e.g.,
            // subscription_created, subscription_updated)

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("[Webhook] Critical error during processing. Payload: {}", payload, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Processing Error");
        }
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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
