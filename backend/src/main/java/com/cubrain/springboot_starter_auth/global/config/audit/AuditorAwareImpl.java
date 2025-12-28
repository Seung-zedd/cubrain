package com.cubrain.springboot_starter_auth.global.config.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.security.Principal;
import java.util.Optional;

/**
 * AuditorAware implementation
 * Returns current authenticated user's name as auditor
 */
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    @NonNull
    @SuppressWarnings("null")
    public Optional<String> getCurrentAuditor() {
        // 1. Get Authentication object from SecurityContextHolder
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                // 2. Check if authenticated and not anonymous
                .filter(auth -> auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                // 3. Get user name from principal
                .map(Principal::getName);
    }
}
