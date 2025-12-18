package com.cubrain.springboot_starter_auth.global.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

public class AuthDto {

    @Builder(access = AccessLevel.PRIVATE)
    public record AuthRequestDto(
            @Schema(description = "User email address", example = "user@example.com") String email) {
        public static AuthRequestDto from(String email) {
            return AuthRequestDto.builder()
                    .email(email)
                    .build();
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record VerifyRequestDto(
            @Schema(description = "User email address", example = "user@example.com") String email,
            @Schema(description = "6-digit verification code", example = "123456") String code) {
        public static VerifyRequestDto of(String email, String code) {
            return VerifyRequestDto.builder()
                    .email(email)
                    .code(code)
                    .build();
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record TokenResponseDto(
            @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken,
            @Schema(description = "JWT Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String refreshToken) {
        public static TokenResponseDto of(String accessToken, String refreshToken) {
            return TokenResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
    }
}
