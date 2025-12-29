package com.cubrain.springboot_starter_auth.domain.card.v1.controller;

import com.cubrain.springboot_starter_auth.domain.card.v1.dto.CardRequestDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.dto.FlashcardResponseDto;
import com.cubrain.springboot_starter_auth.domain.card.v1.service.CardService;
import com.cubrain.springboot_starter_auth.domain.member.Member;
import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.global.usage.UsageLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.cubrain.springboot_starter_auth.domain.user.UserTier.GUEST;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Flashcard generation APIs")
public class CardController {

    private final CardService cardService;
    private final UsageLimitService usageLimitService;
    private final MemberRepository memberRepository;

    @Operation(summary = "Generate Flashcards", description = "Generates a Q&A card from the selected text and context.")
    @PostMapping("/generate")
    public ResponseEntity<FlashcardResponseDto> generate(@RequestBody CardRequestDto request) {
        if (request.selection() == null || request.selection().trim().isEmpty() ||
                request.localContext() == null || request.localContext().trim().isEmpty() ||
                request.globalContext() == null || request.globalContext().trim().isEmpty()) {
            return badRequest().body(null);
        }
        FlashcardResponseDto flashcard = cardService.generateCardDemo(request.selection(), request.localContext(),
                request.globalContext());

        return ok(flashcard);
    }

    @Operation(summary = "Generate Flashcards from PDF", description = "Generates Q&A cards from an uploaded PDF file. Enforces daily limits.")
    @PostMapping("/from-pdf")
    public ResponseEntity<List<FlashcardResponseDto>> generateFromPdf(
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String email = jwt != null ? jwt.getClaimAsString("email") : null;
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        if (email != null) {
            usageLimitService.checkAndIncrement(email);
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            List<FlashcardResponseDto> results = cardService.generateCardsFromPdf(file, member.getTier());
            return ok(results);
        } else {
            usageLimitService.checkAndIncrementGuest(clientIp);
            List<FlashcardResponseDto> results = cardService.generateCardsFromPdf(file, GUEST);
            return ok(results);
        }
    }
}