package com.cubrain.springboot_starter_auth.domain.card.v1;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    Page<Deck> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<Deck> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
