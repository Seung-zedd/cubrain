package com.cubrain.springboot_starter_auth.domain.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import static com.cubrain.springboot_starter_auth.domain.member.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public void incrementDailyUploadCount(String email, LocalDate date) {
        queryFactory.update(member)
                .set(member.dailyUploadCount, member.dailyUploadCount.add(1))
                .set(member.lastUploadDate, date)
                .where(member.email.eq(email))
                .execute();
    }

    @Override
    public void resetAndIncrementDailyUploadCount(String email, LocalDate date) {
        queryFactory.update(member)
                .set(member.dailyUploadCount, 1)
                .set(member.lastUploadDate, date)
                .where(member.email.eq(email))
                .execute();
    }

    @Override
    public void resetDailyUploadCount(String email, LocalDate date) {
        queryFactory.update(member)
                .set(member.dailyUploadCount, 0)
                .set(member.lastUploadDate, date)
                .where(member.email.eq(email))
                .execute();
    }
}
