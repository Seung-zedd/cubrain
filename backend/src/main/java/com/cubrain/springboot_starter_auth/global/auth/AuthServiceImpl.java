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
    public void deleteMember(String email, String supabaseId) {
        log.info("[Auth] Deleting member: {} (sub: {})", email, supabaseId);

        // 1. Try finding by supabaseId
        Optional<Member> memberOpt = memberRepository.findBySupabaseId(supabaseId);

        // 2. Fallback to email if not found by supabaseId
        if (memberOpt.isEmpty() && email != null) {
            log.info("[Auth] Member not found by supabaseId, trying email: {}", email);
            memberOpt = memberRepository.findByEmail(email.toLowerCase());
        }

        memberOpt.ifPresentOrElse(member -> {
            log.info("[Auth] Found member to delete: {} (ID: {})", member.getEmail(), member.getId());
            memberRepository.delete(member);
            memberRepository.flush(); // Force flush to ensure deletion is executed and catch any FK issues
            log.info("[Auth] Member deleted and flushed: {}", member.getEmail());
        }, () -> {
            log.warn("[Auth] No member found to delete for email: {} or sub: {}", email, supabaseId);
        });
    }
}
