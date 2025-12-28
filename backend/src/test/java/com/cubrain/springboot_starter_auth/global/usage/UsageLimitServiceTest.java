package com.cubrain.springboot_starter_auth.global.usage;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageLimitServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JobManager jobManager;

    @InjectMocks
    private UsageLimitService usageLimitService;

    private Member freeMember;
    private Member proMember;

    @BeforeEach
    void setUp() {
        freeMember = Member.builder()
                .email("free@example.com")
                .tier(UserTier.FREE_USER)
                .dailyUploadCount(0)
                .lastUploadDate(LocalDate.now())
                .build();

        proMember = Member.builder()
                .email("pro@example.com")
                .tier(UserTier.PRO_USER)
                .dailyUploadCount(0)
                .lastUploadDate(LocalDate.now())
                .build();
    }

    @Test
    void checkAndIncrement_FreeUser_LimitReached() {
        // Given
        String email = "free@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(freeMember));
        when(jobManager.hasActiveJob(email)).thenReturn(false);

        // Set count to limit
        freeMember.incrementUploadCount();
        freeMember.incrementUploadCount();
        freeMember.incrementUploadCount();

        // When & Then
        assertThrows(IllegalStateException.class, () -> usageLimitService.checkAndIncrement(email));
    }

    @Test
    void checkAndIncrement_FreeUser_UnderLimit() {
        // Given
        String email = "free@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(freeMember));
        when(jobManager.hasActiveJob(email)).thenReturn(false);

        // When
        usageLimitService.checkAndIncrement(email);

        // Then
        verify(memberRepository).incrementDailyUploadCount(eq(email), any(LocalDate.class));
    }

    @Test
    void checkAndIncrement_ProUser_HighLimit() {
        // Given
        String email = "pro@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(proMember));
        when(jobManager.hasActiveJob(email)).thenReturn(false);

        // When
        usageLimitService.checkAndIncrement(email);

        // Then
        verify(memberRepository).incrementDailyUploadCount(eq(email), any(LocalDate.class));
    }

    @Test
    void checkAndIncrement_NewDay_ResetsCount() {
        // Given
        String email = "free@example.com";
        freeMember = Member.builder()
                .email(email)
                .tier(UserTier.FREE_USER)
                .dailyUploadCount(5) // Over limit from yesterday
                .lastUploadDate(LocalDate.now().minusDays(1))
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(freeMember));
        when(jobManager.hasActiveJob(email)).thenReturn(false);

        // When
        usageLimitService.checkAndIncrement(email);

        // Then
        verify(memberRepository).resetAndIncrementDailyUploadCount(eq(email), any(LocalDate.class));
    }
}
