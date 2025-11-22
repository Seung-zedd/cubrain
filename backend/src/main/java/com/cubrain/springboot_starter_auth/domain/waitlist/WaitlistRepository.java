package com.cubrain.springboot_starter_auth.domain.waitlist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<WaitlistUser, Long> {
    boolean existsByEmail(String email);
}