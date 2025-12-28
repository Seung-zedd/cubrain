package com.cubrain.springboot_starter_auth.domain.card.v1;

import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/decks")
@RequiredArgsConstructor
@Tag(name = "Decks", description = "Deck management APIs")
public class DeckController {

    private final CardService cardService;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final MemberRepository memberRepository;

    @Operation(summary = "Get Decks", description = "Retrieves a paginated list of the user's decks with card counts (Semi-Join optimized).")
    @GetMapping
    public ResponseEntity<Page<DeckResponseDto>> getDecks(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20) Pageable pageable) {

        Member member = getMember(jwt);
        Page<Deck> deckPage = deckRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(), pageable);

        List<Long> deckIds = deckPage.getContent().stream()
                .map(Deck::getId)
                .toList();

        Map<Long, Long> cardCounts = flashcardRepository.countByDeckIds(deckIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));

        Page<DeckResponseDto> responsePage = deckPage
                .map(deck -> DeckResponseDto.from(deck, cardCounts.getOrDefault(deck.getId(), 0L)));

        return ResponseEntity.ok(responsePage);
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

        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Save Deck", description = "Saves a new deck with generated flashcards.")
    @PostMapping
    public ResponseEntity<DeckResponseDto> saveDeck(
            @RequestBody DeckCreateRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {

        Member member = getMember(jwt);
        DeckResponseDto savedDeck = cardService.saveDeck(request.title(), request.cards(), member);
        return ResponseEntity.ok(savedDeck);
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

        deckRepository.delete(deck);
        return ResponseEntity.noContent().build();
    }

    private Member getMember(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("Authentication required");
        }
        String email = jwt.getClaimAsString("email");
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
