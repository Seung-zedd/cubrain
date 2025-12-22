package com.cubrain.springboot_starter_auth.global.auth;

public record TokenResponseDto(
    String accessToken,
    String refreshToken
) {
    public static TokenResponseDto of(String accessToken, String refreshToken) {
        return new TokenResponseDto(accessToken, refreshToken);
    }
}
