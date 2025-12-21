package com.cubrain.springboot_starter_auth.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.from}")
    private String fromEmail;

    @Async("emailExecutor")
    public void sendVerificationCode(String to, String code) {
        try {
            log.info("Sending verification code to {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("[Cubrain] Your Verification Code");
            message.setText("Your verification code is: " + code + "\nIt will expire in 5 minutes.");
            mailSender.send(message);
            log.info("Verification code sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification code to {}: {}", to, e.getMessage(), e);
        }
    }
}
