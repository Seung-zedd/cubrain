package com.cubrain.springboot_starter_auth.domain.card.v1.repository;

import com.cubrain.springboot_starter_auth.domain.card.Deck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeckRepositoryCustom {
    Page<Deck> findAllByKeyword(String keyword, Long memberId, Pageable pageable);
}
