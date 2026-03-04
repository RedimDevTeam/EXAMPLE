package com.b2bplatform.wallet.controller;

import com.b2bplatform.wallet.dto.b2b.B2BCancelRequest;
import com.b2bplatform.wallet.dto.b2b.B2BCreditRequest;
import com.b2bplatform.wallet.dto.b2b.B2BDebitRequest;
import com.b2bplatform.wallet.dto.b2b.B2BRefundRequest;
import com.b2bplatform.wallet.dto.response.BalanceResponse;
import com.b2bplatform.wallet.dto.response.CreditResponse;
import com.b2bplatform.wallet.dto.response.DebitResponse;
import com.b2bplatform.wallet.service.B2BSharedWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * B2B Integration Wallet Controller
 * Provides endpoints for B2B Integration with industry-standard field naming
 * 
 * Base Path: /api/v1/b2b/wallet
 */
@RestController
@RequestMapping("/api/v1/b2b/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "B2B Integration - Wallet Operations", description = "B2B Integration wallet APIs with industry-standard naming")
public class B2BWalletController {
    
    private final B2BSharedWalletService b2bSharedWalletService;
    
    /**
     * Debit wallet (bet placement)
     * Uses industry-standard field names: operatorCode, transactionId, gameId, etc.
     */
    @PostMapping("/debit")
    @Operation(summary = "Debit wallet (B2B)", 
               description = "Debit player wallet for bet placement. Uses industry-standard field naming.")
    public ResponseEntity<DebitResponse> debit(@Valid @RequestBody B2BDebitRequest request) {
        log.debug("POST /api/v1/b2b/wallet/debit - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        DebitResponse response = b2bSharedWalletService.debit(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Credit wallet (win payout)
     * Uses industry-standard field names: operatorCode, transactionId, gameId, etc.
     */
    @PostMapping("/credit")
    @Operation(summary = "Credit wallet (B2B)", 
               description = "Credit player wallet for win payout. Uses industry-standard field naming.")
    public ResponseEntity<CreditResponse> credit(@Valid @RequestBody B2BCreditRequest request) {
        log.debug("POST /api/v1/b2b/wallet/credit - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        CreditResponse response = b2bSharedWalletService.credit(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refund transaction
     * Uses industry-standard field names
     */
    @PostMapping("/refund")
    @Operation(summary = "Refund transaction (B2B)", 
               description = "Refund a previous transaction. Uses industry-standard field naming.")
    public ResponseEntity<CreditResponse> refund(@Valid @RequestBody B2BRefundRequest request) {
        log.debug("POST /api/v1/b2b/wallet/refund - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        CreditResponse response = b2bSharedWalletService.refund(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel transaction
     * Uses industry-standard field names
     */
    @PostMapping("/cancel")
    @Operation(summary = "Cancel transaction (B2B)", 
               description = "Cancel a pending transaction. Uses industry-standard field naming.")
    public ResponseEntity<Void> cancel(@Valid @RequestBody B2BCancelRequest request) {
        log.debug("POST /api/v1/b2b/wallet/cancel - operatorCode: {}, transactionId: {}", 
            request.getOperatorCode(), request.getTransactionId());
        b2bSharedWalletService.cancel(request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get balance
     * Uses industry-standard field names (operatorCode instead of operatorId)
     */
    @GetMapping("/balance")
    @Operation(summary = "Get balance (B2B)", 
               description = "Get player wallet balance. Uses industry-standard field naming.")
    public ResponseEntity<BalanceResponse> getBalance(
            @RequestParam String operatorCode,
            @RequestParam String playerId) {
        log.debug("GET /api/v1/b2b/wallet/balance - operatorCode: {}, playerId: {}", 
            operatorCode, playerId);
        BalanceResponse response = b2bSharedWalletService.getBalance(operatorCode, playerId);
        return ResponseEntity.ok(response);
    }
}
