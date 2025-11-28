package com.cubrain.springboot_starter_auth.domain.waitlist;

import com.cubrain.springboot_starter_auth.domain.waitlist.dto.WaitlistRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;

    @Transactional
    @SuppressWarnings("null")
    public String joinWaitlist(WaitlistRequestDto requestDto) {
        String email = requestDto.email();

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("❌ Invalid email format.");
        }

        if (waitlistRepository.existsByEmail(email)) {
            throw new IllegalStateException("⚠️ You are already on the list!");
        }

        waitlistRepository.save(WaitlistUser.builder().email(email).build());
        log.info("🎉 New Waitlist User: {}", email);

        return "✅ You're in! Welcome to the future of study.";
    }
}
