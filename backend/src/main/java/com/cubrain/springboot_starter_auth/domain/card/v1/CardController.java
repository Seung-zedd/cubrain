package com.cubrain.springboot_starter_auth.domain.card.v1;

import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Generate Flashcards", description = "Generates a Q&A card from the selected text and context.")
    @PostMapping("/generate")
    public ResponseEntity<FlashcardResponseDto> generate(@RequestBody CardRequestDto request) {
        if (request.selection() == null || request.selection().trim().isEmpty() ||
                request.localContext() == null || request.localContext().trim().isEmpty() ||
                request.globalContext() == null || request.globalContext().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        FlashcardResponseDto flashcard = cardService.generateCardDemo(request.selection(), request.localContext(),
                request.globalContext());

        return ResponseEntity.ok(flashcard);
    }

    @Operation(summary = "Generate Flashcards from PDF", description = "Generates Q&A cards from an uploaded PDF file. Returns 429 if quota exceeded.")
    @PostMapping("/from-pdf")
    public ResponseEntity<List<FlashcardResponseDto>> generateFromPdf(
            @RequestParam MultipartFile file,
            @RequestParam(value = "userTier", defaultValue = "GUEST") String userTierStr) {

        UserTier userTier;
        try {
            userTier = UserTier.valueOf(userTierStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            userTier = UserTier.GUEST;
        }

        List<FlashcardResponseDto> results = cardService.generateCardsFromPdf(file, userTier);
        return ResponseEntity.ok(results);
    }
}