package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.global.usage.UsageLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth management APIs (Supabase)")
public class AuthController {

    private final AuthService authService;
    private final UsageLimitService usageLimitService;

    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Returns the profile of the currently authenticated user.")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        String email = jwt.getClaimAsString("email");
        String supabaseId = jwt.getSubject(); // 'sub' claim

        log.info("[Auth] getMe called for: {} (sub: {})", email, supabaseId);

        // syncUser handles JIT provisioning and updating supabaseId
        UserResponseDto user = authService.syncUser(email, supabaseId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/usage")
    @Operation(summary = "Get current usage info", description = "Returns the daily upload count for the current user or guest IP.")
    public ResponseEntity<?> getUsage(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        if (jwt != null) {
            String email = jwt.getClaimAsString("email");
            if (email != null) {
                int count = usageLimitService.getUsageCount(email.toLowerCase());
                return ResponseEntity.ok(Map.of("dailyUploadCount", count));
            }
        }

        // Guest logic
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        int count = usageLimitService.getGuestUsageCount(clientIp);
        return ResponseEntity.ok(Map.of("dailyUploadCount", count));
    }
}
