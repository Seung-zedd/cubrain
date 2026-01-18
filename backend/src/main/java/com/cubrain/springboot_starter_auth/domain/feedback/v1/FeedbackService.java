package com.cubrain.springboot_starter_auth.domain.feedback.v1;

public interface FeedbackService {
    void sendFeedbackEmail(FeedbackRequestDto dto, String senderInfo);
}
