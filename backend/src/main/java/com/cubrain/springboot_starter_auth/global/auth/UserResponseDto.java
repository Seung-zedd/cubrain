package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserResponseDto(
        @Schema(description = "User email address", example = "user@example.com") String email,
        @Schema(description = "User role", example = "USER") String role,
        @Schema(description = "User tier", example = "FREE_USER") String tier,
        @Schema(description = "Daily upload count", example = "5") int dailyUploadCount) {
    
    public static UserResponseDto from(Member member) {
        return UserResponseDto.builder()
                .email(member.getEmail())
                .role(member.getRole().name())
                .tier(member.getTier().name())
                .dailyUploadCount(member.getDailyUploadCount())
                .build();
    }
}
