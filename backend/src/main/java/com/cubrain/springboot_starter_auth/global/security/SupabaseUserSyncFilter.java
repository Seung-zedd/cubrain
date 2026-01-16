package com.cubrain.springboot_starter_auth.global.security;

import com.cubrain.springboot_starter_auth.global.auth.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class SupabaseUserSyncFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public SupabaseUserSyncFilter(@Lazy AuthService authService) {
        this.authService = authService;
    }

    private final java.util.Map<String, Long> lastSyncTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long SYNC_INTERVAL_MS = 60000; // 1 minute

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Skip sync for account deletion endpoint to avoid recreation or interference
        if ("DELETE".equalsIgnoreCase(request.getMethod()) && "/api/v1/auth/me".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            String email = jwt.getClaimAsString("email");
            String supabaseId = jwt.getSubject();

            if (email != null && supabaseId != null) {
                long now = System.currentTimeMillis();
                Long lastSync = lastSyncTime.get(supabaseId);

                if (lastSync == null || (now - lastSync) > SYNC_INTERVAL_MS) {
                    authService.syncUser(email, supabaseId);
                    lastSyncTime.put(supabaseId, now);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
