package com.cubrain.springboot_starter_auth.global.webhook;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.member.SubscriptionStatus;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebhookServiceImpl implements WebhookService {

    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void handleSubscriptionEvent(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventName = root.path("meta").path("event_name").asText();
            JsonNode data = root.path("data");
            JsonNode attributes = data.path("attributes");

            String email = attributes.path("user_email").asText();
            String status = attributes.path("status").asText();
            String endsAtStr = attributes.path("ends_at").asText(null);
            String customerPortalUrl = attributes.path("urls").path("customer_portal").asText(null);

            log.info("[Webhook] Processing event: {} for email: {} (status: {}, endsAt: {})",
                    eventName, email, status, endsAtStr);

            if (email.isEmpty()) {
                log.warn("[Webhook] No email found in payload. Skipping processing.");
                return;
            }

            memberRepository.findByEmail(email).ifPresentOrElse(
                    member -> updateMemberSubscription(member, status, endsAtStr, customerPortalUrl),
                    () -> log.warn("[Webhook] Member not found for email: {}", email));

        } catch (Exception e) {
            log.error("[Webhook] Failed to parse or process webhook payload", e);
            throw new RuntimeException("Webhook processing failed", e);
        }
    }

    private void updateMemberSubscription(Member member, String status, String endsAtStr, String customerPortalUrl) {
        UserTier oldTier = member.getTier();
        OffsetDateTime oldEndsAt = member.getEndsAt();

        // Update Customer Portal URL if provided
        if (customerPortalUrl != null && !customerPortalUrl.isEmpty()) {
            member.updateCustomerPortalUrl(customerPortalUrl);
        }

        // Sync Subscription Status
        if (status != null && !status.isEmpty()) {
            try {
                member.updateSubscriptionStatus(SubscriptionStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("[Webhook] Unknown subscription status: {}", status);
            }
        }

        if ("active".equalsIgnoreCase(status)) {
            member.updateTier(UserTier.PRO_USER);
            member.updateEndsAt(null); // Active Pro = null endsAt
        } else if ("cancelled".equalsIgnoreCase(status)) {
            member.updateTier(UserTier.PRO_USER); // Keep as PRO during grace period
            if (endsAtStr != null && !endsAtStr.equals("null")) {
                member.updateEndsAt(OffsetDateTime.parse(endsAtStr));
            }
        } else if ("expired".equalsIgnoreCase(status) || "unpaid".equalsIgnoreCase(status)
                || "past_due".equalsIgnoreCase(status)) {
            member.updateTier(UserTier.FREE_USER);
            // Optionally clear endsAt or keep it
        }

        memberRepository.save(member);
        log.info("[Webhook] Updated member {}: Tier {} -> {}, EndsAt {} -> {}",
                member.getEmail(), oldTier, member.getTier(), oldEndsAt, member.getEndsAt());
    }
}
