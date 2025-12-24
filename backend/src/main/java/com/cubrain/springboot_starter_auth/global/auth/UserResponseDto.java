package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.domain.member.Member;

public record UserResponseDto(
        String email,
        String role,
        String tier,
        int dailyUploadCount) {
    public static UserResponseDto from(Member member) {
        return new UserResponseDto(
                member.getEmail(),
                member.getRole().name(),
                member.getTier().name(),
                member.getDailyUploadCount());
    }
}
