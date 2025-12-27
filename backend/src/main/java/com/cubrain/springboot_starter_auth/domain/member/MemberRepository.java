package com.cubrain.springboot_starter_auth.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findBySupabaseId(String supabaseId);

    boolean existsByEmail(String email);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Member m SET m.dailyUploadCount = m.dailyUploadCount + 1, m.lastUploadDate = :date WHERE m.email = :email")
    void incrementDailyUploadCount(@Param("email") String email, @Param("date") LocalDate date);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Member m SET m.dailyUploadCount = 1, m.lastUploadDate = :date WHERE m.email = :email")
    void resetAndIncrementDailyUploadCount(@Param("email") String email, @Param("date") LocalDate date);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Member m SET m.dailyUploadCount = 0, m.lastUploadDate = :date WHERE m.email = :email")
    void resetDailyUploadCount(@Param("email") String email, @Param("date") LocalDate date);
}
