package com.cubrain.springboot_starter_auth.domain.member;

import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    @DisplayName("FREE_USER should not have Pro access")
    void freeUserNoProAccess() {
        Member member = Member.builder()
                .tier(UserTier.FREE_USER)
                .build();

        assertFalse(member.isProAccess());
    }

    @Test
    @DisplayName("PRO_USER with null endsAt should have Pro access (Active Pro)")
    void proUserNullEndsAtHasAccess() {
        Member member = Member.builder()
                .tier(UserTier.PRO_USER)
                .endsAt(null)
                .build();

        assertTrue(member.isProAccess());
    }

    @Test
    @DisplayName("PRO_USER with future endsAt should have Pro access (Grace Period)")
    void proUserFutureEndsAtHasAccess() {
        Member member = Member.builder()
                .tier(UserTier.PRO_USER)
                .endsAt(OffsetDateTime.now().plusDays(1))
                .build();

        assertTrue(member.isProAccess());
    }

    @Test
    @DisplayName("PRO_USER with past endsAt should not have Pro access (Expired)")
    void proUserPastEndsAtNoAccess() {
        Member member = Member.builder()
                .tier(UserTier.PRO_USER)
                .endsAt(OffsetDateTime.now().minusDays(1))
                .build();

        assertFalse(member.isProAccess());
    }
}
