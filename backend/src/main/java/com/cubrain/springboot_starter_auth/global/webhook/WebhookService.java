package com.cubrain.springboot_starter_auth.global.webhook;

public interface WebhookService {
    void handleSubscriptionEvent(String payload);
}
