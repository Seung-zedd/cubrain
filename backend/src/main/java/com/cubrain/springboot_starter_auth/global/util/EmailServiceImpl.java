package com.cubrain.springboot_starter_auth.global.util;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private Resend resend;

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String fromEmail;

    @PostConstruct
    public void init() {
        this.resend = new Resend(apiKey);
    }

    @Override
    @Async("emailExecutor")
    public void sendVerificationCode(String to, String code) {
        try {
            log.info("Sending verification code to {}", to);

            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("[Cubrain] Your Verification Code")
                    .html("<strong>" + code + "</strong><br>It will expire in 5 minutes.")
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            log.info("Verification code sent successfully to {}. Message ID: {}", to, response.getId());
        } catch (ResendException e) {
            log.error("Failed to send verification code to {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending verification code to {}: {}", to, e.getMessage(), e);
        }
    }
}
