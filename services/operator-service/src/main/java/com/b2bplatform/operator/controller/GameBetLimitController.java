package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateGameBetLimitRequest;
import com.b2bplatform.operator.dto.request.UpdateGameBetLimitRequest;
import com.b2bplatform.operator.dto.response.GameBetLimitResponse;
import com.b2bplatform.operator.service.BetLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing game-specific bet limits.
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/games/{gameId}/bet-limits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Game Bet Limits", description = "APIs for managing game-specific bet limits (Gaming Provider Global Admins only)")
public class GameBetLimitController {
    
    private final BetLimitService betLimitService;
    
    @PostMapping
    @Operation(summary = "Create game-specific bet limit", 
               description = "Create a new bet limit for a game that applies to all operators")
    public ResponseEntity<GameBetLimitResponse> createGameBetLimit(
            @PathVariable String gameId,
            @Valid @RequestBody CreateGameBetLimitRequest request) {
        log.debug("POST /api/v1/admin/games/{}/bet-limits", gameId);
        
        GameBetLimitResponse created = betLimitService.createGameBetLimit(gameId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Get game-specific bet limits", 
               description = "Get bet limits for a game, optionally filtered by currency")
    public ResponseEntity<Map<String, List<GameBetLimitResponse>>> getGameBetLimits(
            @PathVariable String gameId,
            @RequestParam(required = false) String currencyCode,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        log.debug("GET /api/v1/admin/games/{}/bet-limits?currencyCode={}&activeOnly={}", 
            gameId, currencyCode, activeOnly);
        
        List<GameBetLimitResponse> limits = betLimitService.getGameBetLimits(gameId, currencyCode, activeOnly);
        return ResponseEntity.ok(Map.of("limits", limits));
    }
    
    @PutMapping("/{limitId}")
    @Operation(summary = "Update game-specific bet limit", 
               description = "Update an existing game-specific bet limit")
    public ResponseEntity<GameBetLimitResponse> updateGameBetLimit(
            @PathVariable String gameId,
            @PathVariable Long limitId,
            @Valid @RequestBody UpdateGameBetLimitRequest request) {
        log.debug("PUT /api/v1/admin/games/{}/bet-limits/{}", gameId, limitId);
        
        GameBetLimitResponse updated = betLimitService.updateGameBetLimit(gameId, limitId, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{limitId}")
    @Operation(summary = "Deactivate game-specific bet limit", 
               description = "Deactivate a game-specific bet limit, optionally with an effective end date")
    public ResponseEntity<GameBetLimitResponse> deactivateGameBetLimit(
            @PathVariable String gameId,
            @PathVariable Long limitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime effectiveTo) {
        log.debug("DELETE /api/v1/admin/games/{}/bet-limits/{}?effectiveTo={}", gameId, limitId, effectiveTo);
        
        GameBetLimitResponse deactivated = betLimitService.deactivateGameBetLimit(gameId, limitId, effectiveTo);
        return ResponseEntity.ok(deactivated);
    }
}
