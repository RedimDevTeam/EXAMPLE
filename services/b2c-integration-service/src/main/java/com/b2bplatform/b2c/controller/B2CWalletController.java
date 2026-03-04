package com.b2bplatform.b2c.controller;

import com.b2bplatform.b2c.dto.request.*;
import com.b2bplatform.b2c.dto.response.ProviderBalanceResponse;
import com.b2bplatform.b2c.dto.response.ProviderWalletResponse;
import com.b2bplatform.b2c.service.B2CProviderWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for B2C Provider Wallet Operations (JSON)
 * 
 * Base Path: /api/v1/b2c/wallet
 */
@RestController
@RequestMapping("/api/v1/b2c/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "B2C Provider Wallet", description = "B2C provider wallet operations (debit/credit/refund/cancel/balance)")
public class B2CWalletController {
    
    private final B2CProviderWalletService walletService;
    
    /**
     * Debit player wallet
     */
    @PostMapping("/debit")
    @Operation(summary = "Debit player wallet", 
               description = "Debit player wallet via B2C provider API. Requires X-Provider-Id header.")
    public ResponseEntity<ProviderWalletResponse> debit(
            @RequestHeader("X-Provider-Id") String providerId,
            @Valid @RequestBody ProviderDebitRequest request) {
        log.info("POST /api/v1/b2c/wallet/debit - providerId={}, playerId={}", 
            providerId, request.getPlayerId());
        
        ProviderWalletResponse response = walletService.debit(providerId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Credit player wallet
     */
    @PostMapping("/credit")
    @Operation(summary = "Credit player wallet", 
               description = "Credit player wallet via B2C provider API. Requires X-Provider-Id header.")
    public ResponseEntity<ProviderWalletResponse> credit(
            @RequestHeader("X-Provider-Id") String providerId,
            @Valid @RequestBody ProviderCreditRequest request) {
        log.info("POST /api/v1/b2c/wallet/credit - providerId={}, playerId={}", 
            providerId, request.getPlayerId());
        
        ProviderWalletResponse response = walletService.credit(providerId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refund transaction
     */
    @PostMapping("/refund")
    @Operation(summary = "Refund transaction", 
               description = "Refund a previous transaction via B2C provider API. Requires X-Provider-Id header.")
    public ResponseEntity<ProviderWalletResponse> refund(
            @RequestHeader("X-Provider-Id") String providerId,
            @Valid @RequestBody ProviderRefundRequest request) {
        log.info("POST /api/v1/b2c/wallet/refund - providerId={}, originalTransactionId={}", 
            providerId, request.getOriginalTransactionId());
        
        ProviderWalletResponse response = walletService.refund(providerId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel transaction
     */
    @PostMapping("/cancel")
    @Operation(summary = "Cancel transaction", 
               description = "Cancel a pending transaction via B2C provider API. Requires X-Provider-Id header.")
    public ResponseEntity<ProviderWalletResponse> cancel(
            @RequestHeader("X-Provider-Id") String providerId,
            @RequestParam String transactionId,
            @RequestParam String playerId) {
        log.info("POST /api/v1/b2c/wallet/cancel - providerId={}, transactionId={}", 
            providerId, transactionId);
        
        ProviderWalletResponse response = walletService.cancel(providerId, transactionId, playerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get player balance
     */
    @GetMapping("/balance")
    @Operation(summary = "Get player balance", 
               description = "Get player wallet balance via B2C provider API. Requires X-Provider-Id header.")
    public ResponseEntity<ProviderBalanceResponse> getBalance(
            @RequestHeader("X-Provider-Id") String providerId,
            @RequestParam String playerId,
            @RequestParam(required = false) String currency) {
        log.info("GET /api/v1/b2c/wallet/balance - providerId={}, playerId={}", 
            providerId, playerId);
        
        ProviderBalanceRequest request = ProviderBalanceRequest.builder()
                .playerId(playerId)
                .currency(currency)
                .build();
        
        ProviderBalanceResponse response = walletService.getBalance(providerId, request);
        return ResponseEntity.ok(response);
    }
}
