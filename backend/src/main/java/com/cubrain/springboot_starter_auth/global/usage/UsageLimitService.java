package com.cubrain.springboot_starter_auth.global.usage;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UsageLimitService {

    private final MemberRepository memberRepository;
    private final JobManager jobManager;

    // Simple in-memory tracking for Guests (IP -> {count, date})
    private final Map<String, GuestUsage> guestUsageMap = new ConcurrentHashMap<>();

    private static class GuestUsage {
        int count;
        LocalDate date;

        GuestUsage(int count, LocalDate date) {
            this.count = count;
            this.date = date;
        }
    }

    @Transactional
    public void checkAndIncrement(Member member) {
        if (jobManager.hasActiveJob(member.getEmail())) {
            throw new IllegalStateException("You already have an active processing job.");
        }

        member.resetCountIfNewDay();
        int limit = switch (member.getTier()) {
            case FREE_USER -> 3;
            case PRO_USER -> 100; // High limit for Pro
            case GUEST -> 3;
        };

        if (member.getDailyUploadCount() >= limit) {
            throw new IllegalStateException("Daily upload limit reached for " + member.getTier());
        }

        member.incrementUploadCount();
        memberRepository.save(member);
    }

    public void checkAndIncrementGuest(String ip) {
        if (jobManager.hasActiveJob(ip)) {
            throw new IllegalStateException("You already have an active processing job.");
        }

        GuestUsage usage = guestUsageMap.getOrDefault(ip, new GuestUsage(0, LocalDate.now()));

        if (!LocalDate.now().equals(usage.date)) {
            usage = new GuestUsage(0, LocalDate.now());
        }

        if (usage.count >= 3) {
            throw new IllegalStateException("Daily upload limit reached for Guest");
        }

        usage.count++;
        guestUsageMap.put(ip, usage);
    }
}
