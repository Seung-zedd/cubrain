package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.member.Role;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public UserResponseDto syncUser(String email, String supabaseId) {
        String normalizedEmail = email.toLowerCase();

        // 1. Try finding by supabaseId
        Optional<Member> memberBySupabaseId = memberRepository.findBySupabaseId(supabaseId);
        if (memberBySupabaseId.isPresent()) {
            return UserResponseDto.from(memberBySupabaseId.get());
        }

        // 2. Try finding by email
        Optional<Member> memberByEmail = memberRepository.findByEmail(normalizedEmail);
        if (memberByEmail.isPresent()) {
            Member member = memberByEmail.get();
            // Update supabaseId if null
            if (member.getSupabaseId() == null) {
                log.info("[Auth] Updating supabaseId for existing user: {}", normalizedEmail);
                member.updateSupabaseId(supabaseId);
            }
            return UserResponseDto.from(member);
        }

        // 3. Create new member (Just-in-Time Provisioning)
        log.info("[Auth] Creating new member for: {}", normalizedEmail);
        Member newMember = Member.builder()
                .email(normalizedEmail)
                .supabaseId(supabaseId)
                .role(Role.USER)
                .tier(UserTier.FREE_USER)
                .isVerified(true)
                .build();

        return UserResponseDto.from(memberRepository.save(newMember));
    }

    @Override
    @Transactional
    public UserResponseDto getMe(String email) {
        String normalizedEmail = email.toLowerCase();
        Member member = memberRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // Force refresh to ensure we have the latest data from the DB
        entityManager.refresh(member);

        if (member.getLastUploadDate() == null || !LocalDate.now().equals(member.getLastUploadDate())) {
            log.info("[AuthService] New day detected in getMe for user: {}. Resetting count in DB.", normalizedEmail);
            memberRepository.resetDailyUploadCount(normalizedEmail, LocalDate.now());
            member.resetCountIfNewDay();
        }

        return UserResponseDto.from(member);
    }
}
