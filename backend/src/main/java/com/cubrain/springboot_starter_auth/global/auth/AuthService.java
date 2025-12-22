package com.cubrain.springboot_starter_auth.global.auth;

public interface AuthService {
    void requestVerification(String email, AuthMode mode);

    TokenResponseDto verifyAndAuthenticate(String email, String code, AuthMode mode);

    TokenResponseDto refreshTokens(String refreshToken);

    UserResponseDto getMe(String email);
}
