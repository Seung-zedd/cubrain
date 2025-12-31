package com.cubrain.springboot_starter_auth.domain.card.v1.controller;

import com.cubrain.springboot_starter_auth.domain.card.Deck;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.DeckCreateRequestDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.DeckResponseDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardRequestDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.repository.DeckRepository;
import com.cubrain.springboot_starter_auth.domain.card.v1.repository.FlashcardRepository;
import com.cubrain.springboot_starter_auth.domain.card.v1.service.CardService;
import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import org.springframework.web.util.UriUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.cubrain.springboot_starter_auth.domain.card.QFlashcard.flashcard;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/decks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Decks", description = "Deck management APIs")
public class DeckController {

    private final CardService cardService;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final MemberRepository memberRepository;

    @Operation(summary = "Get Decks", description = "Retrieves a paginated list of the user's decks with card counts (Semi-Join optimized). Supports dynamic search by title via the 'keyword' parameter.")
    @GetMapping
    public ResponseEntity<Page<DeckResponseDto>> getDecks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        Member member = getMember(jwt);
        Page<Deck> deckPage = deckRepository.findAllByKeyword(keyword, member.getId(), pageable);

        List<Long> deckIds = deckPage.getContent().stream()
                .map(Deck::getId)
                .toList();

        if (deckIds.isEmpty()) {
            return ok(deckPage.map(deck -> DeckResponseDto.from(deck, 0L)));
        }

        Map<Long, Long> cardCounts = flashcardRepository.countByDeckIds(deckIds).stream()
                .collect(toMap(
                        tuple -> tuple.get(flashcard.deck.id),
                        tuple -> tuple.get(flashcard.count())));

        Page<DeckResponseDto> responsePage = deckPage
                .map(deck -> DeckResponseDto.from(deck, cardCounts.getOrDefault(deck.getId(), 0L)));

        return ok(responsePage);
    }

    @Operation(summary = "Get Deck Cards", description = "Retrieves all flashcards for a specific deck.")
    @GetMapping("/{id}/cards")
    public ResponseEntity<List<FlashcardDto>> getDeckCards(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = getMember(jwt);
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        if (!deck.getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<FlashcardDto> cards = flashcardRepository.findByDeckId(id).stream()
                .map(FlashcardDto::from)
                .toList();

        return ok(cards);
    }

    @Operation(summary = "Save Deck", description = "Saves a new deck with generated flashcards.")
    @PostMapping
    public ResponseEntity<DeckResponseDto> saveDeck(
            @RequestBody DeckCreateRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = getMember(jwt);
        DeckResponseDto savedDeck = cardService.saveDeck(request.title(), request.cards(), member);
        return ok(savedDeck);
    }

    @Operation(summary = "Update Progress", description = "Updates the study progress and last studied time for a deck.")
    @PatchMapping("/{id}/progress")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = getMember(jwt);
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        if (!deck.getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        Integer progress = request.get("progress");
        deck.updateLastStudiedAt(progress);
        deckRepository.save(deck);

        return ok().build();
    }

    @Operation(summary = "Delete Deck", description = "Deletes a deck and all its flashcards.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = getMember(jwt);
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        if (!deck.getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        log.info("🗑️ [API Request] DELETE /api/v1/decks/{} - User: {}", id, member.getEmail());
        cardService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update Deck Title", description = "Updates the title of a specific deck.")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateDeckTitle(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = getMember(jwt);
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        if (!deck.getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        String newTitle = request.get("title");
        if (newTitle == null || newTitle.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("🚀 [API Request] PATCH /api/v1/decks/{} - User: {}", id, member.getEmail());
        cardService.updateDeckTitle(id, newTitle);
        return ok().build();
    }

    @Operation(summary = "Update Deck Cards", description = "Replaces all flashcards in a deck with a new list.")
    @PutMapping("/{id}/cards")
    public ResponseEntity<Void> updateDeckCards(
            @PathVariable Long id,
            @RequestBody List<FlashcardRequestDto> newCards,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = getMember(jwt);
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        if (!deck.getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        log.info("🚀 [API Request] PUT /api/v1/decks/{}/cards - User: {}", id, member.getEmail());
        cardService.updateDeckCards(id, newCards);
        return ok().build();
    }

    @Operation(summary = "Export Deck to Anki", description = "Exports the deck's flashcards to an Anki-compatible CSV format.")
    @GetMapping("/{id}/export/anki")
    public ResponseEntity<String> exportToAnki(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = getMember(jwt);
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        if (!deck.getMember().getId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        log.info("📤 [API Request] GET /api/v1/decks/{}/export/anki - User: {}", id, member.getEmail());
        String csv = cardService.exportToAnki(id);

        String baseFilename = deck.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + "_anki.csv";
        String encodedFilename = UriUtils.encode(baseFilename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    private Member getMember(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("Authentication required");
        }
        String email = jwt.getClaimAsString("email");
        String normalizedEmail = email != null ? email.toLowerCase() : null;
        return memberRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + normalizedEmail));
    }
}
