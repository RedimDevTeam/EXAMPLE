package com.b2bplatform.wallet.controller;

import com.b2bplatform.wallet.dto.b2b.BlockUserRequest;
import com.b2bplatform.wallet.dto.b2b.KickoutRequest;
import com.b2bplatform.wallet.dto.b2b.OperationalResponse;
import com.b2bplatform.wallet.dto.b2b.UnblockUserRequest;
import com.b2bplatform.wallet.dto.response.BalanceResponse;
import com.b2bplatform.wallet.service.B2BOperationalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * B2B Operational Controller
 * Provides operational APIs: block/unblock/kickout/balance
 * 
 * Base Path: /api/v1/b2b/operational
 */
@RestController
@RequestMapping("/api/v1/b2b/operational")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "B2B Integration - Operational APIs", description = "B2B Operational APIs for account management")
public class B2BOperationalController {
    
    private final B2BOperationalService operationalService;
    
    /**
     * Block user account
     */
    @PostMapping("/blockuser")
    @Operation(summary = "Block user account", 
               description = "Suspend/block a player account. Blocked accounts cannot perform transactions.")
    public ResponseEntity<OperationalResponse> blockUser(@Valid @RequestBody BlockUserRequest request) {
        log.debug("POST /api/v1/b2b/operational/blockuser - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        OperationalResponse response = operationalService.blockUser(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unblock user account
     */
    @PostMapping("/unblockuser")
    @Operation(summary = "Unblock user account", 
               description = "Reinstate/unblock a player account. Unblocked accounts can perform transactions again.")
    public ResponseEntity<OperationalResponse> unblockUser(@Valid @RequestBody UnblockUserRequest request) {
        log.debug("POST /api/v1/b2b/operational/unblockuser - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        OperationalResponse response = operationalService.unblockUser(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kickout user (force logout)
     */
    @PostMapping("/kickout")
    @Operation(summary = "Kickout user", 
               description = "Force logout a player. Records kickout timestamp for audit purposes.")
    public ResponseEntity<OperationalResponse> kickout(@Valid @RequestBody KickoutRequest request) {
        log.debug("POST /api/v1/b2b/operational/kickout - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        OperationalResponse response = operationalService.kickout(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get account balance (independent balance check)
     */
    @GetMapping("/accountbalance")
    @Operation(summary = "Get account balance", 
               description = "Independent balance check. Returns account balance. Fails if account is blocked.")
    public ResponseEntity<BalanceResponse> getAccountBalance(
            @RequestParam String operatorCode,
            @RequestParam String playerId) {
        log.debug("GET /api/v1/b2b/operational/accountbalance - operatorCode: {}, playerId: {}", 
            operatorCode, playerId);
        BalanceResponse response = operationalService.getAccountBalance(operatorCode, playerId);
        return ResponseEntity.ok(response);
    }
}
