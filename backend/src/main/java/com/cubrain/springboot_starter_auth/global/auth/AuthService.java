package com.cubrain.springboot_starter_auth.global.auth;

public interface AuthService {
    UserResponseDto syncUser(String email, String firebaseUid);

    void deleteMember(String email, String firebaseUid);
}
