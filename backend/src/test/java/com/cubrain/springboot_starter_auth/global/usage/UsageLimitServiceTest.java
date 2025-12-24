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

import static org.junit.jupiter.api.Assertions.assertThrows;
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
        freeMember = spy(freeMember);
        when(freeMember.getDailyUploadCount()).thenReturn(3);
        when(jobManager.hasActiveJob(anyString())).thenReturn(false);

        // When & Then
        assertThrows(IllegalStateException.class, () -> usageLimitService.checkAndIncrement(freeMember));
    }

    @Test
    void checkAndIncrement_FreeUser_UnderLimit() {
        // Given
        freeMember = spy(freeMember);
        when(freeMember.getDailyUploadCount()).thenReturn(2);
        when(jobManager.hasActiveJob(anyString())).thenReturn(false);

        // When
        usageLimitService.checkAndIncrement(freeMember);

        // Then
        verify(freeMember).incrementUploadCount();
        verify(memberRepository).save(freeMember);
    }

    @Test
    void checkAndIncrement_ProUser_HighLimit() {
        // Given
        proMember = spy(proMember);
        when(proMember.getDailyUploadCount()).thenReturn(50);
        when(jobManager.hasActiveJob(anyString())).thenReturn(false);

        // When
        usageLimitService.checkAndIncrement(proMember);

        // Then
        verify(proMember).incrementUploadCount();
        verify(memberRepository).save(proMember);
    }
}
