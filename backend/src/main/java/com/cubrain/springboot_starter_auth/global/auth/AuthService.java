package com.cubrain.springboot_starter_auth.global.auth;

public interface AuthService {
    UserResponseDto syncUser(String email, String supabaseId);

    void deleteMember(String supabaseId);
}
