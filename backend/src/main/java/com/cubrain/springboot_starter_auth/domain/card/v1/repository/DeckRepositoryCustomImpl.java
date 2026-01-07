package com.cubrain.springboot_starter_auth.domain.card.v1.repository;

import com.cubrain.springboot_starter_auth.domain.card.Deck;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.cubrain.springboot_starter_auth.domain.card.QDeck.deck;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class DeckRepositoryCustomImpl implements DeckRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Deck> findAllByKeyword(String keyword, Long memberId, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // Always filter by memberId
        builder.and(deck.member.id.eq(memberId));

        // Dynamic search by title
        if (hasText(keyword)) {
            builder.and(deck.title.containsIgnoreCase(keyword));
        }

        List<Deck> content = queryFactory
                .selectFrom(deck)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(deck.studyProgress.asc(), deck.createdAt.desc())
                .fetch();

        long total = queryFactory
                .select(deck.count())
                .from(deck)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
