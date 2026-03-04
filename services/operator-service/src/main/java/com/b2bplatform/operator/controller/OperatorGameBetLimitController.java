package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateOperatorGameBetLimitRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorGameBetLimitRequest;
import com.b2bplatform.operator.dto.response.OperatorGameBetLimitResponse;
import com.b2bplatform.operator.service.BetLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing operator-specific bet limits.
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/games/{gameId}/bet-limits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator Game Bet Limits", description = "APIs for managing operator-specific bet limits (Gaming Provider Global Admins only)")
public class OperatorGameBetLimitController {
    
    private final BetLimitService betLimitService;
    
    @PostMapping
    @Operation(summary = "Create operator-specific bet limit", 
               description = "Create a new bet limit for a specific operator and game (overrides game-specific limits)")
    public ResponseEntity<OperatorGameBetLimitResponse> createOperatorGameBetLimit(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @Valid @RequestBody CreateOperatorGameBetLimitRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/games/{}/bet-limits", operatorId, gameId);
        
        OperatorGameBetLimitResponse created = betLimitService.createOperatorGameBetLimit(
            operatorId, gameId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Get operator-specific bet limits", 
               description = "Get bet limits for a specific operator and game, optionally filtered by currency")
    public ResponseEntity<Map<String, List<OperatorGameBetLimitResponse>>> getOperatorGameBetLimits(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @RequestParam(required = false) String currencyCode,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        log.debug("GET /api/v1/admin/operators/{}/games/{}/bet-limits?currencyCode={}&activeOnly={}", 
            operatorId, gameId, currencyCode, activeOnly);
        
        List<OperatorGameBetLimitResponse> limits = betLimitService.getOperatorGameBetLimits(
            operatorId, gameId, currencyCode, activeOnly);
        return ResponseEntity.ok(Map.of("limits", limits));
    }
    
    @PutMapping("/{limitId}")
    @Operation(summary = "Update operator-specific bet limit", 
               description = "Update an existing operator-specific bet limit")
    public ResponseEntity<OperatorGameBetLimitResponse> updateOperatorGameBetLimit(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @PathVariable Long limitId,
            @Valid @RequestBody UpdateOperatorGameBetLimitRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/games/{}/bet-limits/{}", operatorId, gameId, limitId);
        
        OperatorGameBetLimitResponse updated = betLimitService.updateOperatorGameBetLimit(
            operatorId, gameId, limitId, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{limitId}")
    @Operation(summary = "Delete operator-specific bet limit", 
               description = "Delete an operator-specific bet limit (will fall back to game-specific limits)")
    public ResponseEntity<Void> deleteOperatorGameBetLimit(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @PathVariable Long limitId) {
        log.debug("DELETE /api/v1/admin/operators/{}/games/{}/bet-limits/{}", operatorId, gameId, limitId);
        
        betLimitService.deleteOperatorGameBetLimit(operatorId, gameId, limitId);
        return ResponseEntity.noContent().build();
    }
}
