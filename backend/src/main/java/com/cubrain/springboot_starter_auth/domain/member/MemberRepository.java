package com.cubrain.springboot_starter_auth.domain.member;

import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    long countByTier(UserTier tier);
}
