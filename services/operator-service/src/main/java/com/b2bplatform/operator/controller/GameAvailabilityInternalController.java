package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.response.GameAvailabilityResponse;
import com.b2bplatform.operator.service.OperatorGameConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for game availability (used by Bet Service).
 * Provides read-only access to check if games are available for operators.
 */
@RestController
@RequestMapping("/api/v1/internal/game-availability")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Game Availability (Internal)", description = "Internal APIs for checking game availability (used by Bet Service)")
public class GameAvailabilityInternalController {
    
    private final OperatorGameConfigService operatorGameConfigService;
    
    @GetMapping("/{operatorId}/{gameProviderId}/{gameId}")
    @Operation(summary = "Check game availability", 
               description = "Check if a game is available for an operator. Used by Bet Service to validate game availability before processing bets.")
    public ResponseEntity<GameAvailabilityResponse> checkGameAvailability(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @PathVariable String gameId) {
        log.debug("GET /api/v1/internal/game-availability/{}/{}/{}", operatorId, gameProviderId, gameId);
        
        GameAvailabilityResponse availability = operatorGameConfigService.checkGameAvailability(
            operatorId, gameProviderId, gameId);
        return ResponseEntity.ok(availability);
    }
}
