package com.b2bplatform.wallet.controller;

import com.b2bplatform.common.enums.StatusCode;
import com.b2bplatform.common.response.APIResponse;
import com.b2bplatform.wallet.dto.request.CreditRequest;
import com.b2bplatform.wallet.dto.request.DebitRequest;
import com.b2bplatform.wallet.dto.response.BalanceResponse;
import com.b2bplatform.wallet.dto.response.CreditResponse;
import com.b2bplatform.wallet.dto.response.DebitResponse;
import com.b2bplatform.wallet.model.WalletTransaction;
import com.b2bplatform.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet Management", description = "Player wallet operations APIs")
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/debit")
    @Operation(summary = "Debit wallet", description = "Debit player wallet (for bet placement)")
    public ResponseEntity<APIResponse> debit(@Valid @RequestBody DebitRequest request) {
        log.debug("POST /api/v1/wallet/debit - operator: {}, player: {}, amount: {}",
            request.getOperatorId(), request.getPlayerId(), request.getAmount());
        try {
            DebitResponse response = walletService.debit(
                request.getOperatorId(),
                request.getPlayerId(),
                request.getAmount(),
                request.getCurrency(),
                request.getReference(),
                request.getDescription()
            );
            return ResponseEntity.ok(APIResponse.success(response));
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("insufficient") || msg.contains("balance")) {
                return ResponseEntity.ok(APIResponse.get(StatusCode.INSUFFICIENT_BALANCE, e.getMessage()));
            }
            return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, e.getMessage()));
        } catch (Exception e) {
            log.error("Debit error", e);
            return ResponseEntity.ok(APIResponse.get(StatusCode.INTERNAL_SERVER_ERROR, "Debit operation failed"));
        }
    }

    @PostMapping("/credit")
    @Operation(summary = "Credit wallet", description = "Credit player wallet (for win payout)")
    public ResponseEntity<APIResponse> credit(@Valid @RequestBody CreditRequest request) {
        log.debug("POST /api/v1/wallet/credit - operator: {}, player: {}, amount: {}",
            request.getOperatorId(), request.getPlayerId(), request.getAmount());
        try {
            CreditResponse response = walletService.credit(
                request.getOperatorId(),
                request.getPlayerId(),
                request.getAmount(),
                request.getCurrency(),
                request.getReference(),
                request.getDescription()
            );
            return ResponseEntity.ok(APIResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, e.getMessage()));
        } catch (Exception e) {
            log.error("Credit error", e);
            return ResponseEntity.ok(APIResponse.get(StatusCode.INTERNAL_SERVER_ERROR, "Credit operation failed"));
        }
    }

    @GetMapping("/balance")
    @Operation(summary = "Get balance", description = "Query player wallet balance")
    public ResponseEntity<APIResponse> getBalance(
            @RequestParam Long operatorId,
            @RequestParam String playerId) {
        log.debug("GET /api/v1/wallet/balance - operator: {}, player: {}", operatorId, playerId);
        try {
            BalanceResponse response = walletService.getBalance(operatorId, playerId);
            return ResponseEntity.ok(APIResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, e.getMessage()));
        } catch (Exception e) {
            log.error("Get balance error", e);
            return ResponseEntity.ok(APIResponse.get(StatusCode.INTERNAL_SERVER_ERROR, "Balance query failed"));
        }
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history", description = "Get wallet transaction history for a player")
    public ResponseEntity<APIResponse> getTransactionHistory(
            @RequestParam String playerId,
            @RequestParam(required = false) Integer limit) {
        log.debug("GET /api/v1/wallet/transactions - player: {}, limit: {}", playerId, limit);
        try {
            List<WalletTransaction> transactions = walletService.getTransactionHistory(playerId, limit);
            return ResponseEntity.ok(APIResponse.success(transactions));
        } catch (Exception e) {
            log.error("Get transaction history error", e);
            return ResponseEntity.ok(APIResponse.get(StatusCode.INTERNAL_SERVER_ERROR, "Failed to get transactions"));
        }
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get transaction", description = "Get transaction details by transaction ID")
    public ResponseEntity<APIResponse> getTransaction(@PathVariable String transactionId) {
        log.debug("GET /api/v1/wallet/transactions/{}", transactionId);
        try {
            Optional<WalletTransaction> transaction = walletService.getTransaction(transactionId);
            if (transaction.isPresent()) {
                return ResponseEntity.ok(APIResponse.success(transaction.get()));
            }
            return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, "Transaction not found"));
        } catch (Exception e) {
            log.error("Get transaction error", e);
            return ResponseEntity.ok(APIResponse.get(StatusCode.INTERNAL_SERVER_ERROR, "Failed to get transaction"));
        }
    }
}
