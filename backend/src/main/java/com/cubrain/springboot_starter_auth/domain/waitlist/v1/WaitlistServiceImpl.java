package com.cubrain.springboot_starter_auth.domain.waitlist.v1;

import com.cubrain.springboot_starter_auth.domain.waitlist.WaitlistRepository;
import com.cubrain.springboot_starter_auth.domain.waitlist.WaitlistUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistServiceImpl implements WaitlistService {

    private final WaitlistRepository waitlistRepository;

    @Override
    @Transactional
    public String joinWaitlist(WaitlistRequestDto requestDto) {
        String email = requestDto.email();

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("❌ Invalid email format.");
        }

        String normalizedEmail = email.toLowerCase();

        if (waitlistRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("⚠️ You are already on the list!");
        }

        WaitlistUser user = WaitlistUser.builder().email(normalizedEmail).build();
        waitlistRepository.save(Objects.requireNonNull(user));
        log.info("🎉 New Waitlist User: {}", normalizedEmail);

        return "✅ You're in! Welcome to the future of study.";
    }
}
