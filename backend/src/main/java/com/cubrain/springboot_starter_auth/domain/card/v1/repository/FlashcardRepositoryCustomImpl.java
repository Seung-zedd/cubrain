package com.cubrain.springboot_starter_auth.domain.card.v1.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.cubrain.springboot_starter_auth.domain.card.QFlashcard.flashcard;

@Repository
@RequiredArgsConstructor
public class FlashcardRepositoryCustomImpl implements FlashcardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Tuple> countByDeckIds(List<Long> deckIds) {
        return queryFactory
                .select(flashcard.deck.id, flashcard.count())
                .from(flashcard)
                .where(flashcard.deck.id.in(deckIds))
                .groupBy(flashcard.deck.id)
                .fetch();
    }
}
