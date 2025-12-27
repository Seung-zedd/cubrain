package com.cubrain.springboot_starter_auth.global.auth;

import java.util.Optional;

public interface AuthService {
    UserResponseDto syncUser(String email, String supabaseId);

    UserResponseDto getMe(String email);
}
