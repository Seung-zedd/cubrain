package com.cubrain.springboot_starter_auth.global.config.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;

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
        // TODO: 인증 기능 추가 시 실제 사용자 정보 반환
        // 현재는 시스템 사용자로 고정
        return Optional.of("system");
    }
}
