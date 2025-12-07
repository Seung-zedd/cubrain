package com.cubrain.springboot_starter_auth.domain.card;

import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;

    @PostMapping("/generate")
    public ResponseEntity<FlashcardResponseDto> generate(@RequestBody Map<String, String> payload) {
        String selection = payload.get("selection");
        String localContext = payload.get("localContext");
        String globalContext = payload.get("globalContext");

        // Input validation
        if (selection == null || selection.trim().isEmpty() ||
                localContext == null || localContext.trim().isEmpty() ||
                globalContext == null || globalContext.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        // Call Service
        FlashcardResponseDto flashcard = cardService.generateCardDemo(selection, localContext, globalContext);

        return ResponseEntity.ok(flashcard);
    }

    @PostMapping("/from-pdf")
    public ResponseEntity<List<FlashcardResponseDto>> generateFromPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userTier", defaultValue = "GUEST") String userTierStr) {

        UserTier userTier;
        try {
            userTier = UserTier.valueOf(userTierStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            userTier = UserTier.GUEST; // Fallback
        }

        List<FlashcardResponseDto> results = cardService.generateCardsFromPdf(file, userTier);
        return ResponseEntity.ok(results);
    }
}