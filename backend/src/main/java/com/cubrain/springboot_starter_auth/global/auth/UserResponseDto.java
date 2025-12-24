package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDto(
        @Schema(description = "User email address", example = "user@example.com") String email,
        @Schema(description = "User role", example = "USER") String role,
        @Schema(description = "User tier", example = "FREE_USER") String tier,
        @Schema(description = "Daily upload count", example = "5") int dailyUploadCount) {
    public static UserResponseDto from(Member member) {
        return new UserResponseDto(
                member.getEmail(),
                member.getRole().name(),
                member.getTier().name(),
                member.getDailyUploadCount());
    }
}
