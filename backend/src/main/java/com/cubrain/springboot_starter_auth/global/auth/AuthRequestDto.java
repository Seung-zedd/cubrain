package com.cubrain.springboot_starter_auth.global.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email
) {}
