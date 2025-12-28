package com.cubrain.springboot_starter_auth.domain.card.v1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByDeckId(Long deckId);

    @Query("SELECT f.deck.id, COUNT(f) FROM Flashcard f WHERE f.deck.id IN :deckIds GROUP BY f.deck.id")
    List<Object[]> countByDeckIds(@Param("deckIds") List<Long> deckIds);
}
