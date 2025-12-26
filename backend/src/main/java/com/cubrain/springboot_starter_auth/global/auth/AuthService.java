package com.cubrain.springboot_starter_auth.global.auth;

import java.util.Optional;

public interface AuthService {
    Optional<TokenResponseDto> requestVerification(String email, AuthMode mode, String refreshToken);

    TokenResponseDto verifyAndAuthenticate(String email, String code, AuthMode mode);

    TokenResponseDto refreshTokens(String refreshToken);

    UserResponseDto getMe(String email);
}
