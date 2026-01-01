package com.cubrain.springboot_starter_auth.global.usage;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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
    public void checkAndIncrement(String email) {
        String normalizedEmail = email.toLowerCase();
        if (jobManager.hasActiveJob(normalizedEmail)) {
            throw new IllegalStateException("You already have an active processing job.");
        }

        Member member = memberRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + normalizedEmail));

        log.info("[UsageLimit] Found member: {}, currentCount: {}, lastUploadDate: {}, tier: {}",
                normalizedEmail, member.getDailyUploadCount(), member.getLastUploadDate(), member.getTier());

        member.resetCountIfNewDay();
        int currentCount = member.getDailyUploadCount();

        int limit = switch (member.getTier()) {
            case FREE_USER -> 3;
            case PRO_USER -> 100;
            case GUEST -> 3;
        };

        if (currentCount >= limit) {
            throw new IllegalStateException("Daily upload limit reached for " + member.getTier());
        }

        member.incrementUploadCount();
        memberRepository.saveAndFlush(member);
        log.info("[UsageLimit] Incremented count for {}. New count: {}", normalizedEmail, member.getDailyUploadCount());
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

    @Transactional
    public void decrementUsage(String email) {
        String normalizedEmail = email.toLowerCase();
        memberRepository.findByEmail(normalizedEmail).ifPresent(member -> {
            member.decrementUploadCount();
            memberRepository.saveAndFlush(member);
            log.info("[UsageLimit] Decremented count for {}. New count: {}", normalizedEmail,
                    member.getDailyUploadCount());
        });
    }

    public void decrementGuestUsage(String ip) {
        GuestUsage usage = guestUsageMap.get(ip);
        if (usage != null && usage.count > 0) {
            usage.count--;
            log.info("[UsageLimit] Decremented guest count for {}. New count: {}", ip, usage.count);
        }
    }

    public int getUsageCount(String email) {
        String normalizedEmail = email.toLowerCase();
        return memberRepository.findByEmail(normalizedEmail)
                .map(member -> {
                    member.resetCountIfNewDay();
                    return member.getDailyUploadCount();
                })
                .orElse(0);
    }

    public int getGuestUsageCount(String ip) {
        GuestUsage usage = guestUsageMap.get(ip);
        if (usage == null || !LocalDate.now().equals(usage.date)) {
            return 0;
        }
        return usage.count;
    }
}
