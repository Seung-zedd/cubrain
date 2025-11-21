package com.example.springboot_starter_auth.global.util;

/**
 * Security 유틸리티 클래스
 * 현재는 인증 기능이 없으므로 기본값 반환
 * 추후 인증 기능 추가 시 실제 사용자 정보를 반환하도록 수정 필요
 */
public class SecurityUtils {

    private SecurityUtils() {}  // 인스턴스화 방지

    /**
     * 현재 사용자의 닉네임 반환
     * TODO: 인증 기능 추가 시 실제 사용자 닉네임 반환
     * @return 현재는 "anonymous" 반환
     */
    public static String getCurrentNickname() {
        // TODO: 인증 기능 추가 시 실제 사용자 정보 반환
        return "anonymous";
    }
}
