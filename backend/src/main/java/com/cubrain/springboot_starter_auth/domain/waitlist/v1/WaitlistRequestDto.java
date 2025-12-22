package com.cubrain.springboot_starter_auth.domain.waitlist.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record WaitlistRequestDto(
        @Schema(description = "User's email address", example = "user@example.com") @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @jakarta.validation.constraints.Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must contain a valid domain (e.g., user@example.com)") String email) {
}
