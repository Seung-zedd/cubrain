package com.cubrain.springboot_starter_auth.global.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponseDto(
        @Schema(description = "JWT access token") String accessToken,
        @Schema(description = "JWT refresh token") String refreshToken) {
    public static TokenResponseDto of(String accessToken, String refreshToken) {
        return new TokenResponseDto(accessToken, refreshToken);
    }
}
