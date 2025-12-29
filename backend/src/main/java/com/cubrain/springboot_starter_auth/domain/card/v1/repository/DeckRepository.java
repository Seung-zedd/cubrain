package com.cubrain.springboot_starter_auth.domain.card.v1.repository;

import com.cubrain.springboot_starter_auth.domain.card.Deck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long>, DeckRepositoryCustom {

    Page<Deck> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<Deck> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
