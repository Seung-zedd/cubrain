package com.cubrain.springboot_starter_auth.global.webhook;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

            log.info("[Webhook] Processing event: {} for email: {} (status: {})", eventName, email, status);

            if (email.isEmpty()) {
                log.warn("[Webhook] No email found in payload. Skipping processing.");
                return;
            }

            memberRepository.findByEmail(email).ifPresentOrElse(
                    member -> updateMemberTier(member, eventName, status),
                    () -> log.warn("[Webhook] Member not found for email: {}", email));

        } catch (Exception e) {
            log.error("[Webhook] Failed to parse or process webhook payload", e);
            throw new RuntimeException("Webhook processing failed", e);
        }
    }

    private void updateMemberTier(Member member, String eventName, String status) {
        UserTier oldTier = member.getTier();
        UserTier newTier = oldTier;

        // Logic based on Lemon Squeezy event names and subscription status
        if (eventName.startsWith("subscription_")) {
            if ("active".equalsIgnoreCase(status)) {
                newTier = UserTier.PRO_USER;
            } else if ("expired".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)
                    || "unpaid".equalsIgnoreCase(status)) {
                newTier = UserTier.FREE_USER;
            }
        }

        if (oldTier != newTier) {
            member.updateTier(newTier);
            memberRepository.save(member);
            log.info("[Webhook] Updated member {} tier: {} -> {}", member.getEmail(), oldTier, newTier);
        } else {
            log.info("[Webhook] No tier change required for member {}. Current tier: {}", member.getEmail(), oldTier);
        }
    }
}
