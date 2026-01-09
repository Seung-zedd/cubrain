package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import java.time.OffsetDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record UserResponseDto(
        @Schema(description = "User ID", example = "1") Long id,
        @Schema(description = "User email address", example = "user@example.com") String email,
        @Schema(description = "User role", example = "USER") String role,
        @Schema(description = "User tier", example = "FREE_USER") String tier,
        @Schema(description = "Daily upload count", example = "5") int dailyUploadCount,
        @Schema(description = "Subscription end date", example = "2026-02-10T15:00:00Z") OffsetDateTime endsAt,
        @Schema(description = "Customer portal URL", example = "https://billing.lemonsqueezy.com/...") String customerPortalUrl) {

    public static UserResponseDto from(Member member) {
        return UserResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .role(member.getRole().name())
                .tier(member.getTier().name())
                .dailyUploadCount(member.getDailyUploadCount())
                .endsAt(member.getEndsAt())
                .customerPortalUrl(member.getCustomerPortalUrl())
                .build();
    }
}
