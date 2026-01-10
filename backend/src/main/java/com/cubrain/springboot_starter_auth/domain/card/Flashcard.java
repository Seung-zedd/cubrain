package com.cubrain.springboot_starter_auth.domain.card;

import com.cubrain.springboot_starter_auth.global.config.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "flashcards")
public class Flashcard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column
    private Integer page;

    public void updateContent(String question, String answer, Integer page) {
        this.question = question;
        this.answer = answer;
        this.page = page;
    }
}
