package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.response.EffectiveBetLimitResponse;
import com.b2bplatform.operator.service.BetLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for bet limits (used by Bet Service).
 * Provides read-only access to effective bet limits with resolution logic.
 */
@RestController
@RequestMapping("/api/v1/internal/bet-limits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bet Limits (Internal)", description = "Internal APIs for retrieving effective bet limits (used by Bet Service)")
public class BetLimitInternalController {
    
    private final BetLimitService betLimitService;
    
    @GetMapping("/{operatorId}/{gameId}/{currencyCode}")
    @Operation(summary = "Get effective bet limits", 
               description = "Get effective bet limits for an operator, game, and currency. " +
                             "Resolves in priority: operator-specific → game-specific → system default")
    public ResponseEntity<EffectiveBetLimitResponse> getEffectiveBetLimit(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @PathVariable String currencyCode) {
        log.debug("GET /api/v1/internal/bet-limits/{}/{}/{}", operatorId, gameId, currencyCode);
        
        EffectiveBetLimitResponse limits = betLimitService.getEffectiveBetLimit(
            operatorId, gameId, currencyCode);
        return ResponseEntity.ok(limits);
    }
}
