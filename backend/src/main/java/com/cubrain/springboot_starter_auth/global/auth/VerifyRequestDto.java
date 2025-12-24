package com.cubrain.springboot_starter_auth.global.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRequestDto(
                @Schema(description = "User email address", example = "user@example.com") @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

                @Schema(description = "6-digit verification code", example = "123456") @NotBlank(message = "Verification code is required") @Size(min = 6, max = 6, message = "Verification code must be 6 digits") String code) {
}
