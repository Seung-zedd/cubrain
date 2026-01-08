package com.cubrain.springboot_starter_auth.global.config.lemonsqueezy;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

@Slf4j
@Builder
@ConfigurationProperties(prefix = "lemonsqueezy")
public record LemonSqueezyProperties(
        String productId,
        String checkoutUrl,
        Variants variants,
        Webhook webhook) {
    @Builder
    public record Variants(
            String monthly,
            String yearly) {
    }

    @Builder
    public record Webhook(
            String secret) {
    }

    public LemonSqueezyProperties {
        log.info("🛠️ [LemonSqueezy] Validating configuration...");
        if (Objects.isNull(productId) || productId.isBlank()) {
            log.warn("⚠️ [LemonSqueezy] product-id is missing or empty!");
        }
        if (Objects.isNull(checkoutUrl) || checkoutUrl.isBlank()) {
            log.warn("⚠️ [LemonSqueezy] checkout-url is missing or empty!");
        }
        if (Objects.isNull(variants) || Objects.isNull(variants.monthly()) || variants.monthly().isBlank()) {
            log.warn("⚠️ [LemonSqueezy] variants.monthly is missing or empty!");
        }
        if (Objects.isNull(variants) || Objects.isNull(variants.yearly()) || variants.yearly().isBlank()) {
            log.warn("⚠️ [LemonSqueezy] variants.yearly is missing or empty!");
        }
        if (Objects.isNull(webhook) || Objects.isNull(webhook.secret()) || webhook.secret().isBlank()) {
            log.warn("⚠️ [LemonSqueezy] webhook.secret is missing or empty!");
        }
    }
}
