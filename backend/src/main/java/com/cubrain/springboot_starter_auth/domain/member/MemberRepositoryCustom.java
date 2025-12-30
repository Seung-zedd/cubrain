package com.cubrain.springboot_starter_auth.domain.member;

import java.time.LocalDate;

public interface MemberRepositoryCustom {
    void incrementDailyUploadCount(String email, LocalDate date);

    void resetAndIncrementDailyUploadCount(String email, LocalDate date);

    void resetDailyUploadCount(String email, LocalDate date);
}
