package com.cubrain.springboot_starter_auth.domain.card.v1.service;

import com.cubrain.springboot_starter_auth.domain.card.Deck;
import com.cubrain.springboot_starter_auth.domain.card.Flashcard;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.DeckResponseDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardRequestDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardResponseDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.repository.DeckRepository;
import com.cubrain.springboot_starter_auth.domain.card.v1.repository.FlashcardRepository;
import com.cubrain.springboot_starter_auth.domain.job.v1.JobManager;
import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private final JobManager jobManager;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardGenerator flashcardGenerator;

    @Qualifier("pdfProcessingExecutor")
    private final Executor pdfProcessingExecutor;

    @Override
    public String generateCardsAsync(MultipartFile file, UserTier userTier, String ownerId) {
        String jobId = jobManager.createJob(ownerId);

        CompletableFuture.runAsync(() -> {
            try {
                List<FlashcardResponseDto> results = flashcardGenerator.generateCardsFromPdf(file, userTier, jobId);
                jobManager.completeJob(jobId, results);
            } catch (Exception e) {
                log.error("Async job failed", e);
                jobManager.failJob(jobId);
            }
        }, pdfProcessingExecutor);

        return jobId;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext) {
        return flashcardGenerator.generateCardDemo(selection, localContext, globalContext);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
            String annotationType) {
        return flashcardGenerator.generateCardDemo(selection, localContext, globalContext, annotationType);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FlashcardResponseDto generateCardDemo(String selection, String localContext, String globalContext,
            String annotationType, String targetLanguage) {
        return flashcardGenerator.generateCardDemo(selection, localContext, globalContext, annotationType,
                targetLanguage);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier) {
        return flashcardGenerator.generateCardsFromPdf(file, userTier);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<FlashcardResponseDto> generateCardsFromPdf(MultipartFile file, UserTier userTier, String jobId) {
        return flashcardGenerator.generateCardsFromPdf(file, userTier, jobId);
    }

    @Override
    @Transactional
    public DeckResponseDto saveDeck(String title, List<FlashcardResponseDto> cards, Member member) {
        Deck deck = Deck.builder()
                .title(title)
                .member(member)
                .build();

        List<Flashcard> flashcards = cards.stream()
                .map(dto -> Flashcard.builder()
                        .deck(deck)
                        .question(dto.question())
                        .answer(dto.answer())
                        .build())
                .toList();

        deck.getCards().addAll(flashcards);
        Deck savedDeck = deckRepository.save(deck);

        return DeckResponseDto.from(savedDeck, flashcards.size());
    }

    @Override
    @Transactional
    public void updateDeckTitle(Long deckId, String newTitle) {
        log.info("📝 [Transaction Start] Updating title for deck ID: {} to '{}'", deckId, newTitle);
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));
        deck.updateTitle(newTitle);
        deckRepository.save(deck);
        log.info("✅ [Transaction Success] Deck title updated.");
    }

    @Override
    @Transactional
    public void updateDeckCards(Long deckId, List<FlashcardRequestDto> newCards) {
        log.info("🗂️ [Transaction Start] Updating cards for deck ID: {}. New card count: {}", deckId, newCards.size());
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        // Remove cards not in the new list
        List<Long> newCardIds = newCards.stream()
                .map(FlashcardRequestDto::id)
                .filter(java.util.Objects::nonNull)
                .toList();

        int removedCount = deck.getCards().size();
        deck.getCards().removeIf(card -> !newCardIds.contains(card.getId()));
        removedCount -= deck.getCards().size();
        log.info("🗑️ Removed {} cards from deck ID: {}", removedCount, deckId);

        // Update existing or add new
        int updatedCount = 0;
        int addedCount = 0;
        for (FlashcardRequestDto dto : newCards) {
            if (dto.id() != null) {
                deck.getCards().stream()
                        .filter(card -> card.getId().equals(dto.id()))
                        .findFirst()
                        .ifPresent(card -> card.updateContent(dto.question(), dto.answer()));
                updatedCount++;
            } else {
                deck.getCards().add(Flashcard.builder()
                        .deck(deck)
                        .question(dto.question())
                        .answer(dto.answer())
                        .build());
                addedCount++;
            }
        }
        log.info("✨ Processed {} updates and {} new cards for deck ID: {}", updatedCount, addedCount, deckId);

        deckRepository.save(deck);
        log.info("✅ [Transaction Success] Deck cards synchronized.");
    }

    @Override
    @Transactional
    public void deleteDeck(Long deckId) {
        log.info("🗑️ [Transaction Start] Deleting deck ID: {}", deckId);
        flashcardRepository.deleteByDeckId(deckId);
        deckRepository.deleteById(deckId);
        log.info("✅ [Transaction Success] Deck and associated cards deleted.");
    }

    @Override
    public String exportToAnki(Long deckId) {
        log.info("📤 Exporting deck ID: {} to Anki CSV", deckId);
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        // Prepend UTF-8 BOM (\uFEFF) for Excel compatibility with non-ASCII characters
        StringBuilder csv = new StringBuilder("\uFEFF");
        for (Flashcard card : deck.getCards()) {
            String question = sanitizeForCsv(card.getQuestion());
            String answer = sanitizeForCsv(card.getAnswer());
            csv.append(question).append(";").append(answer).append("\n");
        }

        return csv.toString();
    }

    private String sanitizeForCsv(String text) {
        if (text == null) {
            return "";
        }
        // 1. Convert newlines to <br> (Anki compatibility)
        String sanitized = text.replace("\n", "<br>").replace("\r", "");

        // 2. Escape double quotes by doubling them (" -> "")
        sanitized = sanitized.replace("\"", "\"\"");

        // 3. Wrap in double quotes to preserve semicolons and commas
        return "\"" + sanitized + "\"";
    }
}
