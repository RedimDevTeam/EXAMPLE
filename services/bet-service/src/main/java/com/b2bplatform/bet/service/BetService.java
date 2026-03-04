package com.b2bplatform.bet.service;

import com.b2bplatform.bet.client.GameServiceClient;
import com.b2bplatform.bet.client.WalletServiceClient;
import com.b2bplatform.bet.dto.BetRequest;
import com.b2bplatform.bet.dto.BetResponse;
import com.b2bplatform.bet.dto.SettlementRequest;
import com.b2bplatform.bet.exception.BetNotFoundException;
import com.b2bplatform.bet.exception.BetRejectedException;
import com.b2bplatform.bet.exception.BetSettlementException;
import com.b2bplatform.bet.exception.BetValidationException;
import com.b2bplatform.bet.model.Bet;
import com.b2bplatform.bet.model.BetStatus;
import com.b2bplatform.bet.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BetService {
    
    private final BetRepository betRepository;
    private final BetValidationService betValidationService;
    private final WalletServiceClient walletServiceClient;
    private final GameServiceClient gameServiceClient;
    private final BetPlacementService betPlacementService;
    private final BetIdempotencyService betIdempotencyService;
    
    /**
     * Place a bet with CRITICAL operator confirmation flow.
     * Bet must be confirmed by operator BEFORE game starts.
     */
    @Transactional
    public Mono<BetResponse> placeBet(BetRequest request, Long playerId, Long operatorId) {
        return betPlacementService.placeBetWithOperatorConfirmation(request, playerId, operatorId);
    }
    
    /**
     * Legacy method - kept for backward compatibility.
     * Use placeBet() which uses the new critical flow.
     */
    @Deprecated
    @Transactional
    public Mono<BetResponse> placeBetLegacy(BetRequest request, Long playerId, Long operatorId) {
        log.info("Placing bet - game: {}, betType: {}, amount: {}, player: {}", 
            request.getGameCode(), request.getBetType(), request.getBetAmount(), playerId);
        
        // 1. Validate generic bet rules
        try {
            betValidationService.validateGenericBetRules(request);
        } catch (BetValidationException e) {
            return Mono.error(e);
        }
        
        // 2. Call Game Service to validate game-specific bet type and get odds
        return Mono.just(gameServiceClient.validateBet(request.getGameCode(), request))
            .flatMap(gameValidation -> {
                if (!gameValidation.getValid()) {
                    return Mono.error(new BetValidationException(
                        gameValidation.getMessage() != null ? gameValidation.getMessage() : "Bet validation failed"));
                }
                
                // Check bet amount against game limits
                if (request.getBetAmount().compareTo(gameValidation.getMaxBetAmount()) > 0) {
                    return Mono.error(new BetValidationException(
                        String.format("Bet amount exceeds game maximum: %.2f", gameValidation.getMaxBetAmount())));
                }
                
                if (request.getBetAmount().compareTo(gameValidation.getMinBetAmount()) < 0) {
                    return Mono.error(new BetValidationException(
                        String.format("Bet amount below game minimum: %.2f", gameValidation.getMinBetAmount())));
                }
                
                // 3. Generate bet ID and transaction reference
                String betId = generateBetId();
                String transactionReference = "BET_" + betId;
                
                // 4. Debit wallet
                return Mono.just(walletServiceClient.debit(
                    operatorId,
                    String.valueOf(playerId),
                    request.getBetAmount(),
                    request.getCurrency(),
                    transactionReference,
                    String.format("Bet: %s - %s", request.getGameCode(), request.getBetType())
                ))
                .flatMap(debitResponse -> {
                    Boolean success = (Boolean) debitResponse.get("success");
                    if (success == null || !success) {
                        String error = (String) debitResponse.getOrDefault("error", "Wallet debit failed");
                        log.error("Wallet debit failed: {}", error);
                        return Mono.error(new BetRejectedException("Wallet debit failed: " + error));
                    }
                    
                    String walletTransactionId = (String) debitResponse.get("transactionId");
                    
                    // 5. Save bet with odds from Game Service
                    Bet bet = Bet.builder()
                        .betId(betId)
                        .playerId(playerId)
                        .operatorId(operatorId)
                        .gameCode(request.getGameCode())
                        .gameRoundId(request.getGameRoundId())
                        .betCategory(request.getBetCategory())
                        .betType(request.getBetType())
                        .betAmount(request.getBetAmount())
                        .currency(request.getCurrency())
                        .odds(gameValidation.getOdds())
                        .status(BetStatus.ACCEPTED)
                        .walletTransactionId(walletTransactionId)
                        .betDetails(request.getBetDetails())
                        .build();
                    
                    Bet savedBet = betRepository.save(bet);
                    log.info("Bet placed successfully - betId: {}, player: {}, amount: {}", 
                        betId, playerId, request.getBetAmount());
                    
                    return Mono.just(BetResponse.from(savedBet));
                });
            })
            .onErrorMap(throwable -> {
                if (throwable instanceof BetValidationException || throwable instanceof BetRejectedException) {
                    return throwable;
                }
                log.error("Error placing bet: {}", throwable.getMessage(), throwable);
                return new BetRejectedException("Failed to place bet: " + throwable.getMessage());
            });
    }
    
    /**
     * Get bet by bet ID
     */
    public BetResponse getBet(String betId) {
        Bet bet = betRepository.findByBetId(betId)
            .orElseThrow(() -> new BetNotFoundException("Bet not found: " + betId));
        return BetResponse.from(bet);
    }
    
    /**
     * Get player bet history
     */
    public java.util.List<BetResponse> getPlayerBets(Long playerId) {
        return betRepository.findByPlayerId(playerId).stream()
            .map(BetResponse::from)
            .toList();
    }
    
    /**
     * Settle a bet: validate → credit wallet → update bet status
     * 
     * IDEMPOTENCY: Prevents duplicate settlements due to network retries.
     */
    @Transactional
    public Mono<BetResponse> settleBet(String betId, SettlementRequest settlementRequest) {
        log.info("Settling bet - betId: {}, result: {}, payout: {}", 
            betId, settlementRequest.getResult(), settlementRequest.getPayoutAmount());
        
        // IDEMPOTENCY CHECK: Generate settlement reference and check if already processed
        String settlementReference = "SETTLE_" + betId + "_" + settlementRequest.getResult() + "_" + 
            settlementRequest.getPayoutAmount().toString();
        
        // Get bet first (needed for validation and idempotency check)
        Bet bet = betRepository.findByBetId(betId)
            .orElseThrow(() -> new BetNotFoundException("Bet not found: " + betId));
        
        // IDEMPOTENCY: Check if already settled (database is source of truth)
        if (bet.getStatus() == BetStatus.SETTLED) {
            log.warn("Bet already settled - betId: {}, returning existing result (idempotent)", betId);
            // Verify idempotency key exists (should exist if properly settled)
            if (!betIdempotencyService.isSettlementAlreadyProcessed(betId, settlementReference)) {
                // Bet is settled but idempotency key missing - store it now
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    String settlementResponseJson = objectMapper.writeValueAsString(BetResponse.from(bet));
                    betIdempotencyService.markSettlementAsProcessed(betId, settlementReference, settlementResponseJson);
                } catch (Exception e) {
                    log.warn("Failed to store missing settlement idempotency key: {}", e.getMessage());
                }
            }
            return Mono.just(BetResponse.from(bet));
        }
        
        // Check Redis idempotency key (for concurrent requests - double-check)
        if (betIdempotencyService.isSettlementAlreadyProcessed(betId, settlementReference)) {
            log.warn("Duplicate settlement request detected - betId: {}, reference: {}, re-checking database", 
                betId, settlementReference);
            // Re-check database (might have been settled between Redis check and DB read)
            Bet recheckedBet = betRepository.findByBetId(betId)
                .orElseThrow(() -> new BetNotFoundException("Bet not found: " + betId));
            if (recheckedBet.getStatus() == BetStatus.SETTLED) {
                log.info("Bet was settled concurrently - betId: {}, returning existing result", betId);
                return Mono.just(BetResponse.from(recheckedBet));
            }
            // Redis has it but DB doesn't - possible race condition, log warning but proceed
            log.warn("Settlement idempotency key exists but bet not settled - betId: {}, possible race condition, proceeding", betId);
        }
        
        // Validate bet can be settled
        if (bet.getStatus() != BetStatus.ACCEPTED) {
            return Mono.error(new BetSettlementException(
                String.format("Bet %s cannot be settled. Current status: %s", betId, bet.getStatus())));
        }
        
        // Create final references for use in lambda (must be effectively final)
        final Bet finalBet = bet;
        final String finalBetId = betId;
        final SettlementRequest finalSettlementRequest = settlementRequest;
        final String finalSettlementReference = settlementReference;
        
        // Credit wallet if payout > 0
        Mono<Map<String, Object>> creditMono;
        if (finalSettlementRequest.getPayoutAmount().compareTo(BigDecimal.ZERO) > 0) {
            String transactionReference = "SETTLE_" + finalBetId;
            creditMono = Mono.just(walletServiceClient.credit(
                finalBet.getOperatorId(),
                String.valueOf(finalBet.getPlayerId()),
                finalSettlementRequest.getPayoutAmount(),
                finalBet.getCurrency(),
                transactionReference,
                String.format("Bet settlement: %s - %s", finalBet.getGameCode(), finalSettlementRequest.getResult())
            ));
        } else {
            creditMono = Mono.just(Map.of("success", true));
        }
        
        return creditMono
            .flatMap(creditResponse -> {
                Boolean success = (Boolean) creditResponse.get("success");
                if (success == null || !success) {
                    String error = (String) creditResponse.getOrDefault("error", "Wallet credit failed");
                    log.error("Wallet credit failed: {}", error);
                    return Mono.error(new BetSettlementException("Wallet credit failed: " + error));
                }
                
                // Update bet status
                finalBet.setStatus(BetStatus.SETTLED);
                finalBet.setPayoutAmount(finalSettlementRequest.getPayoutAmount());
                finalBet.setSettledAt(LocalDateTime.now());
                
                Bet savedBet = betRepository.save(finalBet);
                log.info("Bet settled successfully - betId: {}, payout: {}", finalBetId, finalSettlementRequest.getPayoutAmount());
                
                // CRITICAL: Mark settlement as processed (idempotency) - prevents duplicate settlements
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    String settlementResponseJson = objectMapper.writeValueAsString(BetResponse.from(savedBet));
                    betIdempotencyService.markSettlementAsProcessed(finalBetId, finalSettlementReference, settlementResponseJson);
                    log.debug("Settlement idempotency key stored - betId: {}, reference: {}", finalBetId, finalSettlementReference);
                } catch (Exception e) {
                    log.error("CRITICAL: Failed to store settlement idempotency key - betId: {}, error: {}", finalBetId, e.getMessage());
                    // Log error but continue - database is source of truth
                }
                
                return Mono.just(BetResponse.from(savedBet));
            })
            .onErrorMap(throwable -> {
                if (throwable instanceof BetSettlementException) {
                    return throwable;
                }
                log.error("Error settling bet: {}", throwable.getMessage(), throwable);
                return new BetSettlementException("Failed to settle bet: " + throwable.getMessage());
            });
    }
    
    /**
     * Cancel a bet: validate → refund wallet → update bet status
     */
    @Transactional
    public Mono<BetResponse> cancelBet(String betId) {
        log.info("Cancelling bet - betId: {}", betId);
        
        Bet bet = betRepository.findByBetId(betId)
            .orElseThrow(() -> new BetNotFoundException("Bet not found: " + betId));
        
        // Validate bet can be cancelled
        if (bet.getStatus() != BetStatus.ACCEPTED && bet.getStatus() != BetStatus.PENDING) {
            // Check if already cancelled (idempotency)
            if (bet.getStatus() == BetStatus.CANCELLED) {
                log.warn("Bet already cancelled - betId: {}, returning existing result", betId);
                return Mono.just(BetResponse.from(bet));
            }
            return Mono.error(new BetRejectedException(
                String.format("Bet %s cannot be cancelled. Current status: %s", betId, bet.getStatus())));
        }
        
        // Refund wallet (sync client)
        String transactionReference = "CANCEL_" + betId;
        return Mono.just(walletServiceClient.credit(
            bet.getOperatorId(),
            String.valueOf(bet.getPlayerId()),
            bet.getBetAmount(),
            bet.getCurrency(),
            transactionReference,
            String.format("Bet cancellation: %s", bet.getGameCode())
        ))
        .flatMap(creditResponse -> {
            Boolean success = (Boolean) creditResponse.get("success");
            if (success == null || !success) {
                String error = (String) creditResponse.getOrDefault("error", "Wallet credit failed");
                log.error("Wallet refund failed: {}", error);
                return Mono.error(new BetRejectedException("Wallet refund failed: " + error));
            }
            
            // Update bet status
            bet.setStatus(BetStatus.CANCELLED);
            Bet savedBet = betRepository.save(bet);
            log.info("Bet cancelled successfully - betId: {}", betId);
            
            return Mono.just(BetResponse.from(savedBet));
        })
        .onErrorMap(throwable -> {
            if (throwable instanceof BetRejectedException) {
                return throwable;
            }
            log.error("Error cancelling bet: {}", throwable.getMessage(), throwable);
            return new BetRejectedException("Failed to cancel bet: " + throwable.getMessage());
        });
    }
    
    /**
     * Generate unique bet ID
     */
    private String generateBetId() {
        return "BET_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
