package com.cubrain.springboot_starter_auth.domain.waitlist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record WaitlistRequestDto(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email
) {}
