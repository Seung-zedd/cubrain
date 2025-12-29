package com.cubrain.springboot_starter_auth.domain.card.v1.repository;

import com.cubrain.springboot_starter_auth.domain.card.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long>, FlashcardRepositoryCustom {

    List<Flashcard> findByDeckId(Long deckId);

}
