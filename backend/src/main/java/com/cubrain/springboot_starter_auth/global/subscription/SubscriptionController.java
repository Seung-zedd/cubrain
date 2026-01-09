package com.cubrain.springboot_starter_auth.global.subscription;

import com.cubrain.springboot_starter_auth.domain.member.MemberRepository;
import com.cubrain.springboot_starter_auth.domain.user.UserTier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Subscription and Early Bird management APIs")
public class SubscriptionController {

    private final MemberRepository memberRepository;
    private static final int EARLY_BIRD_LIMIT = 100;

    @GetMapping("/early-bird-count")
    @Operation(summary = "Get remaining early bird spots", description = "Returns the number of remaining spots for the 72% lifetime discount.")
    public ResponseEntity<Map<String, Integer>> getEarlyBirdCount() {
        long currentProCount = memberRepository.countByTier(UserTier.PRO_USER);
        int remainingSpots = Math.max(0, EARLY_BIRD_LIMIT - (int) currentProCount);
        return ResponseEntity.ok(Map.of("remainingSpots", remainingSpots));
    }
}
