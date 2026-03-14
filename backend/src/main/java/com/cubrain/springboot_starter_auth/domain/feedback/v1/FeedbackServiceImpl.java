package com.cubrain.springboot_starter_auth.domain.feedback.v1;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private final String resendApiKey;

    public FeedbackServiceImpl(@Value("${RESEND_API_KEY:dummy_key}") String resendApiKey) {
        this.resendApiKey = resendApiKey;
    }

    @Override
    public void sendFeedbackEmail(FeedbackRequestDto dto, String senderInfo) {
        Resend resend = new Resend(resendApiKey);

        String htmlContent = String.format(
                "<h3>[New Feedback Received]</h3>" +
                        "<ul>" +
                        "  <li><strong>Sender:</strong> %s</li>" +
                        "  <li><strong>Goals:</strong> %s</li>" +
                        "  <li><strong>Pain Points:</strong> %s</li>" +
                        "  <li><strong>Message:</strong> %s</li>" +
                        "</ul>",
                senderInfo,
                String.join(", ", dto.goals()),
                String.join(", ", dto.painPoints()),
                dto.shortFeedback());

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Cubrain <support@cubrain.app>")
                .to("support@cubrain.app")
                .subject("[User Feedback] Survey v2 Response")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("✅ Feedback email sent successfully. ID: {}", data.getId());
        } catch (ResendException e) {
            log.error("❌ Failed to send feedback email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send feedback email", e);
        }
    }
}
