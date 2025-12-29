package com.cubrain.springboot_starter_auth.domain.card.v1;

import com.cubrain.springboot_starter_auth.domain.card.Deck;
import com.cubrain.springboot_starter_auth.domain.card.Flashcard;
import com.cubrain.springboot_starter_auth.domain.card.v1.repository.DeckRepository;
import com.cubrain.springboot_starter_auth.domain.card.v1.repository.FlashcardRepository;
import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.global.config.jpa.QuerydslConfig;
import com.querydsl.core.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.cubrain.springboot_starter_auth.domain.card.QFlashcard.flashcard;
import static com.cubrain.springboot_starter_auth.domain.member.Role.USER;
import static com.cubrain.springboot_starter_auth.domain.user.UserTier.FREE_USER;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
class DeckRepositoryTest {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("test@example.com")
                .role(USER)
                .tier(FREE_USER)
                .build();

        memberRepository.save(member);

        Deck deck1 = Deck.builder()
                .title("Java Basics")
                .member(member)
                .build();
        deckRepository.save(deck1);

        Deck deck2 = Deck.builder()
                .title("Spring Boot")
                .member(member)
                .build();
        deckRepository.save(deck2);

        Flashcard card1 = Flashcard.builder()
                .question("What is JVM?")
                .answer("Java Virtual Machine")
                .deck(deck1)
                .build();

        flashcardRepository.save(card1);

        Flashcard card2 = Flashcard.builder()
                .question("What is JRE?")
                .answer("Java Runtime Environment")
                .deck(deck1)
                .build();

        flashcardRepository.save(card2);
    }

    @Test
    @DisplayName("Should find decks by keyword")
    void findAllByKeyword() {
        // When
        Page<Deck> result = deckRepository.findAllByKeyword("Java", member.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Basics");
    }

    @Test
    @DisplayName("Should return all decks when keyword is empty")
    void findAllByKeyword_Empty() {
        // When
        Page<Deck> result = deckRepository.findAllByKeyword("", member.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should count flashcards by deck IDs using QueryDSL")
    void countByDeckIds() {
        // Given
        List<Long> deckIds = deckRepository.findAll().stream().map(Deck::getId).toList();

        // When
        List<Tuple> counts = flashcardRepository.countByDeckIds(deckIds);

        // Then
        assertThat(counts).hasSize(1); // Only deck1 has cards
        Tuple tuple = counts.get(0);
        assertThat(tuple.get(flashcard.count())).isEqualTo(2L);
    }
}
