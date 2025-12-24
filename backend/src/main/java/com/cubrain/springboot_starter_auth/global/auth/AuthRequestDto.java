package com.cubrain.springboot_starter_auth.global.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(
        @Schema(description = "User email address", example = "user@example.com") @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
}
