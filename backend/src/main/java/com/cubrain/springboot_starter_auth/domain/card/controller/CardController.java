package com.cubrain.springboot_starter_auth.domain.card.controller;

import com.cubrain.springboot_starter_auth.domain.card.dto.FlashcardResponseDto;
import com.cubrain.springboot_starter_auth.domain.card.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;

    @PostMapping("/generate")
    public ResponseEntity<FlashcardResponseDto> generate(@RequestBody Map<String, String> payload) {
        String text = payload.get("selectedText");
        
        // Call Service
        FlashcardResponseDto flashcard = cardService.generateCard(text);
        
        return ResponseEntity.ok(flashcard);
    }
}