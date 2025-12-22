package com.cubrain.springboot_starter_auth.global.auth;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.member.Role;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.cubrain.springboot_starter_auth.global.jwt.JwtTokenProvider;
import com.cubrain.springboot_starter_auth.global.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository verificationRepository;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void requestVerification(String email, AuthMode mode) {
        boolean exists = memberRepository.existsByEmail(email);

        if (mode == AuthMode.SIGN_IN && !exists) {
            throw new IllegalArgumentException("Account not found. Please sign up below.");
        }
        if (mode == AuthMode.SIGN_UP && exists) {
            throw new IllegalArgumentException("Account already exists. Please sign in.");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        verificationRepository.save(Objects.requireNonNull(verification));
        emailService.sendVerificationCode(email, code);
    }

    @Override
    @Transactional
    public TokenResponseDto verifyAndAuthenticate(String email, String code, AuthMode mode) {
        EmailVerification verification = verificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Verification code not found."));

        if (verification.isExpired()) {
            verificationRepository.delete(verification);
            throw new IllegalArgumentException("Verification code expired.");
        }
        if (!verification.getCode().equals(code)) {
            throw new IllegalArgumentException("Please enter the accurate verification code.");
        }

        verificationRepository.delete(verification);

        Member member;
        if (mode == AuthMode.SIGN_UP) {
            member = Member.builder()
                    .email(email)
                    .role(Role.USER)
                    .tier(UserTier.FREE_USER)
                    .isVerified(true)
                    .build();
            @SuppressWarnings("null")
            Member savedMember = memberRepository.save(member);
            member = savedMember;
        } else {
            member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found."));
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return TokenResponseDto.of(accessToken, refreshToken);
    }

    @Override
    public TokenResponseDto refreshTokens(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return TokenResponseDto.of(newAccessToken, newRefreshToken);
    }
}
