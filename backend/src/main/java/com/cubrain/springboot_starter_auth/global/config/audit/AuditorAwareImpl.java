package com.cubrain.springboot_starter_auth.global.config.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.security.Principal;
import java.util.Optional;

/**
 * AuditorAware 구현체
 * 현재는 인증 기능이 없으므로 "system"을 기본 auditor로 반환
 * 추후 인증 기능 추가 시 실제 사용자 정보를 반환하도록 수정 필요
 */
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    @NonNull
    @SuppressWarnings("null")
    public Optional<String> getCurrentAuditor() {
        // 1. SecurityContextHolder에서 Authentication 객체를 바로 Optional로 가져옵니다.
        //* 현재 로그인한 사용자의 인증 정보 조회
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                // 2. 인증 정보가 있고, 인증된 상태(isAnonymous()가 아님)인지 확인합니다.
                .filter(auth -> auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                // 3. 인증 정보에서 사용자 이름(principal.getName())을 가져옵니다.
                .map(Principal::getName);
    }
}
