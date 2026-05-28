package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.member.Role;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public UserResponseDto syncUser(String email, String firebaseUid) {
        String normalizedEmail = email.toLowerCase();

        // 1. Try finding by firebaseUid
        Optional<Member> memberByFirebaseUid = memberRepository.findByFirebaseUid(firebaseUid);
        if (memberByFirebaseUid.isPresent()) {
            return UserResponseDto.from(memberByFirebaseUid.get());
        }

        // 2. Try finding by email
        Optional<Member> memberByEmail = memberRepository.findByEmail(normalizedEmail);
        if (memberByEmail.isPresent()) {
            Member member = memberByEmail.get();
            // Update firebaseUid if null
            if (member.getFirebaseUid() == null) {
                log.info("[Auth] Updating firebaseUid for existing user: {}", normalizedEmail);
                member.updateFirebaseUid(firebaseUid);
            }
            return UserResponseDto.from(member);
        }

        // 3. Create new member (Just-in-Time Provisioning)
        log.info("[Auth] Creating new member for: {}", normalizedEmail);
        Member newMember = Member.builder()
                .email(normalizedEmail)
                .firebaseUid(firebaseUid)
                .role(Role.USER)
                .tier(UserTier.FREE_USER)
                .isVerified(true)
                .build();

        Member savedMember = Objects.requireNonNull(memberRepository.save(newMember));
        return UserResponseDto.from(savedMember);
    }

    @Override
    @Transactional
    public void deleteMember(String email, String firebaseUid) {
        log.info("[Auth] Deleting member: {} (sub: {})", email, firebaseUid);

        // 1. Try finding by firebaseUid
        Optional<Member> memberOpt = memberRepository.findByFirebaseUid(firebaseUid);

        // 2. Fallback to email if not found by firebaseUid
        if (memberOpt.isEmpty() && email != null) {
            log.info("[Auth] Member not found by firebaseUid, trying email: {}", email);
            memberOpt = memberRepository.findByEmail(email.toLowerCase());
        }

        memberOpt.ifPresentOrElse(member -> {
            log.info("[Auth] Found member to delete: {} (ID: {})", member.getEmail(), member.getId());
            memberRepository.delete(member);
            memberRepository.flush(); // Force flush to ensure deletion is executed and catch any FK issues
            log.info("[Auth] Member deleted and flushed: {}", member.getEmail());
        }, () -> {
            log.warn("[Auth] No member found to delete for email: {} or sub: {}", email, firebaseUid);
        });
    }
}
