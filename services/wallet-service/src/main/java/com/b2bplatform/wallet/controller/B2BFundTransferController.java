package com.b2bplatform.wallet.controller;

import com.b2bplatform.wallet.dto.b2b.FundTransferConfirmRequest;
import com.b2bplatform.wallet.dto.b2b.FundTransferConfirmResponse;
import com.b2bplatform.wallet.dto.b2b.FundTransferRequest;
import com.b2bplatform.wallet.dto.b2b.FundTransferRequestResponse;
import com.b2bplatform.wallet.dto.b2b.FundTransferStatusResponse;
import com.b2bplatform.wallet.service.B2BFundTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * B2B Fund Transfer Controller
 * Provides endpoints for Fund Transfer two-step flow (Request → Confirm)
 * 
 * Base Path: /api/v1/b2b/fund
 */
@RestController
@RequestMapping("/api/v1/b2b/fund")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "B2B Integration - Fund Transfer", description = "B2B Fund Transfer two-step flow APIs")
public class B2BFundTransferController {
    
    private final B2BFundTransferService fundTransferService;
    
    /**
     * Step 1: Request Fund Transfer
     * Creates a pending transaction that must be confirmed
     */
    @PostMapping("/request")
    @Operation(summary = "Request fund transfer (Step 1)", 
               description = "Create a pending fund transfer transaction. Returns paymentId for confirmation.")
    public ResponseEntity<FundTransferRequestResponse> requestFund(
            @Valid @RequestBody FundTransferRequest request) {
        log.debug("POST /api/v1/b2b/fund/request - operatorCode: {}, playerId: {}, type: {}", 
            request.getOperatorCode(), request.getPlayerId(), request.getTransactionType());
        FundTransferRequestResponse response = fundTransferService.requestFund(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Step 2: Confirm Fund Transfer
     * Confirms a pending transaction and processes the fund transfer
     */
    @PostMapping("/confirm")
    @Operation(summary = "Confirm fund transfer (Step 2)", 
               description = "Confirm a pending fund transfer transaction and process the transfer.")
    public ResponseEntity<FundTransferConfirmResponse> confirmFund(
            @Valid @RequestBody FundTransferConfirmRequest request) {
        log.debug("POST /api/v1/b2b/fund/confirm - paymentId: {}, transactionId: {}", 
            request.getPaymentId(), request.getTransactionId());
        FundTransferConfirmResponse response = fundTransferService.confirmFund(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get status of pending fund transfer
     */
    @GetMapping("/status/{paymentId}")
    @Operation(summary = "Get fund transfer status", 
               description = "Get the status of a pending fund transfer transaction.")
    public ResponseEntity<FundTransferStatusResponse> getStatus(
            @PathVariable String paymentId) {
        log.debug("GET /api/v1/b2b/fund/status/{}", paymentId);
        FundTransferStatusResponse response = fundTransferService.getStatus(paymentId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all pending transactions for operator and player
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending transactions", 
               description = "Get all pending fund transfer transactions for an operator and player.")
    public ResponseEntity<List<FundTransferStatusResponse>> getPendingTransactions(
            @RequestParam String operatorCode,
            @RequestParam String playerId) {
        log.debug("GET /api/v1/b2b/fund/pending - operatorCode: {}, playerId: {}", 
            operatorCode, playerId);
        List<FundTransferStatusResponse> response = fundTransferService.getPendingTransactions(operatorCode, playerId);
        return ResponseEntity.ok(response);
    }
}
