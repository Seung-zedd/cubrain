package com.cubrain.springboot_starter_auth.global.util;

public interface EmailService {
    void sendVerificationCode(String to, String code);
}
