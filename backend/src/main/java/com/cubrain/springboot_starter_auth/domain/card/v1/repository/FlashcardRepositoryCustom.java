package com.cubrain.springboot_starter_auth.domain.card.v1.repository;

import com.querydsl.core.Tuple;
import java.util.List;

public interface FlashcardRepositoryCustom {
    List<Tuple> countByDeckIds(List<Long> deckIds);
}
