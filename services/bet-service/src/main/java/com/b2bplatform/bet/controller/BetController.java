package com.b2bplatform.bet.controller;

import com.b2bplatform.bet.dto.BetRequest;
import com.b2bplatform.bet.dto.BetResponse;
import com.b2bplatform.bet.dto.SettlementRequest;
import com.b2bplatform.bet.service.BetService;
import com.b2bplatform.common.enums.StatusCode;
import com.b2bplatform.common.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bet Management", description = "Bet placement, settlement, and history APIs")
public class BetController {
    
    private final BetService betService;
    
    @PostMapping
    @Operation(summary = "Place a bet", description = "Place a bet on a game. Requires JWT authentication.")
    public ResponseEntity<APIResponse> placeBet(
            @Valid @RequestBody BetRequest request,
            @RequestHeader("X-Player-Id") Long playerId,
            @RequestHeader("X-Operator-Id") Long operatorId) {
        log.debug("POST /api/v1/bets - player: {}, operator: {}, game: {}, betType: {}",
            playerId, operatorId, request.getGameCode(), request.getBetType());
        try {
            BetResponse bet = betService.placeBet(request, playerId, operatorId).block();
            return ResponseEntity.ok(APIResponse.success(bet));
        } catch (Exception e) {
            log.warn("Place bet error: {}", e.getMessage());
            StatusCode code = e.getMessage() != null && e.getMessage().toLowerCase().contains("insufficient")
                ? StatusCode.INSUFFICIENT_BALANCE : StatusCode.INVALID_REQUEST;
            return ResponseEntity.ok(APIResponse.get(code, e.getMessage()));
        }
    }

    @GetMapping("/{betId}")
    @Operation(summary = "Get bet details", description = "Get bet details by bet ID")
    public ResponseEntity<APIResponse> getBet(@PathVariable String betId) {
        log.debug("GET /api/v1/bets/{}", betId);
        try {
            BetResponse bet = betService.getBet(betId);
            return ResponseEntity.ok(APIResponse.success(bet));
        } catch (Exception e) {
            log.warn("Get bet error: {}", e.getMessage());
            return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/player/{playerId}")
    @Operation(summary = "Get player bet history", description = "Get all bets for a player")
    public ResponseEntity<APIResponse> getPlayerBets(@PathVariable Long playerId) {
        log.debug("GET /api/v1/bets/player/{}", playerId);
        try {
            List<BetResponse> bets = betService.getPlayerBets(playerId);
            return ResponseEntity.ok(APIResponse.success(bets));
        } catch (Exception e) {
            log.warn("Get player bets error: {}", e.getMessage());
            return ResponseEntity.ok(APIResponse.get(StatusCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/{betId}")
    @Operation(summary = "Cancel a bet", description = "Cancel a pending or accepted bet")
    public ResponseEntity<APIResponse> cancelBet(@PathVariable String betId) {
        log.debug("DELETE /api/v1/bets/{}", betId);
        try {
            BetResponse bet = betService.cancelBet(betId).block();
            return ResponseEntity.ok(APIResponse.success(bet));
        } catch (Exception e) {
            log.warn("Cancel bet error: {}", e.getMessage());
            return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/{betId}/settle")
    @Operation(summary = "Settle a bet", description = "Settle a bet (internal endpoint, called by Game Service)")
    public ResponseEntity<APIResponse> settleBet(
            @PathVariable String betId,
            @Valid @RequestBody SettlementRequest settlementRequest) {
        log.debug("POST /api/v1/bets/{}/settle - result: {}, payout: {}",
            betId, settlementRequest.getResult(), settlementRequest.getPayoutAmount());
        try {
            BetResponse bet = betService.settleBet(betId, settlementRequest).block();
            return ResponseEntity.ok(APIResponse.success(bet));
        } catch (Exception e) {
            log.warn("Settle bet error: {}", e.getMessage());
            return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, e.getMessage()));
        }
    }
}
