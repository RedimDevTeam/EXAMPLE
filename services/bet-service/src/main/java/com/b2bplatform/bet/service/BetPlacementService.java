package com.b2bplatform.bet.service;

import com.b2bplatform.bet.client.GameServiceClient;
import com.b2bplatform.bet.client.OperatorServiceClient;
import com.b2bplatform.bet.dto.BetRequest;
import com.b2bplatform.bet.dto.BetResponse;
import com.b2bplatform.bet.exception.BetRejectedException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service handling CRITICAL bet placement flow:
 * 1. Post bet to operator BEFORE game starts
 * 2. Wait for operator confirmation BEFORE game starts
 * 3. Auto-reject if game starts before operator confirmation
 * 4. Handle operator responses: success, low balance, not a player, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BetPlacementService {
    
    private final BetRepository betRepository;
    private final BetValidationService betValidationService;
    private final GameServiceClient gameServiceClient;
    private final OperatorServiceClient operatorServiceClient;
    private final OperatorWebhookService operatorWebhookService;
    private final BetIdempotencyService betIdempotencyService;
    
    /**
     * CRITICAL: Place bet with operator confirmation BEFORE game starts.
     * 
     * Flow:
     * 1. Validate bet (generic + game-specific)
     * 2. Check game round state (betting must be open)
     * 3. Save bet as PENDING
     * 4. Post bet to operator webhook
     * 5. Wait for operator response (with timeout)
     * 6. Check if game started while waiting
     * 7. If game started before response → auto-reject as NOT_CONFIRMED
     * 8. Process operator response (success, low balance, not a player, etc.)
     */
    @Transactional
    public Mono<BetResponse> placeBetWithOperatorConfirmation(
            BetRequest request, Long playerId, Long operatorId) {
        
        log.info("CRITICAL: Placing bet with operator confirmation - game: {}, round: {}, betType: {}, amount: {}, player: {}", 
            request.getGameCode(), request.getGameRoundId(), request.getBetType(), request.getBetAmount(), playerId);
        
        LocalDateTime betPlacedAt = LocalDateTime.now();
        String betId = generateBetId(request, playerId, operatorId);
        
        // 0. IDEMPOTENCY CHECK: Check if bet was already processed (prevent duplicate posts)
        if (betIdempotencyService.isBetAlreadyProcessed(betId)) {
            log.warn("Duplicate bet request detected - betId: {} already processed, returning existing result", betId);
            // Check database first (source of truth)
            java.util.Optional<Bet> existingBet = betRepository.findByBetId(betId);
            if (existingBet.isPresent()) {
                log.info("Returning existing bet from database - betId: {}, status: {}", betId, existingBet.get().getStatus());
                return Mono.just(BetResponse.from(existingBet.get()));
            }
            // If Redis has it but DB doesn't, return error (data inconsistency)
            return Mono.error(new BetRejectedException(
                "Duplicate bet request detected but bet not found in database. Possible data inconsistency."));
        }
        
        // 1. Validate generic bet rules
        try {
            betValidationService.validateGenericBetRules(request);
        } catch (BetValidationException e) {
            return Mono.error(e);
        }
        
        // 2. Validate game-specific bet type and get odds (sync client)
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
                
                // 3. Check game round state (betting must be open)
                return Mono.just(gameServiceClient.checkGameRoundState(request.getGameCode(), request.getGameRoundId()))
                    .flatMap(gameState -> {
                        if (!gameState.isBettingOpen() || gameState.isGameStarted()) {
                            return Mono.error(new BetRejectedException(
                                "Betting window is closed. Game has already started or betting is not open."));
                        }
                        
                        // 4. Double-check idempotency: Check if bet with same ID already exists in database
                        // (Redis check was done earlier, but database is source of truth)
                        java.util.Optional<Bet> existingBet = betRepository.findByBetId(betId);
                        if (existingBet.isPresent()) {
                            log.warn("Bet already exists in database - betId: {}, status: {}, returning existing bet", 
                                betId, existingBet.get().getStatus());
                            // Update idempotency key if missing
                            if (!betIdempotencyService.isBetAlreadyProcessed(betId)) {
                                try {
                                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                                    String betResponseJson = objectMapper.writeValueAsString(BetResponse.from(existingBet.get()));
                                    betIdempotencyService.markBetAsProcessed(betId, betResponseJson);
                                } catch (Exception e) {
                                    log.warn("Failed to store missing bet idempotency key: {}", e.getMessage());
                                }
                            }
                            return Mono.just(BetResponse.from(existingBet.get()));
                        }
                        
                        // 5. Save bet as PENDING (waiting for operator confirmation)
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
                            .status(BetStatus.PENDING)
                            .betDetails(request.getBetDetails())
                            .operatorPostedAt(betPlacedAt)
                            .build();
                        
                        Bet savedBet = betRepository.save(bet);
                        log.info("Bet saved as PENDING - betId: {}, waiting for operator confirmation", betId);
                        
                        // CRITICAL: Mark bet as being processed (idempotency) - prevents duplicate posts to operator
                        // Store immediately after saving to DB to prevent concurrent requests
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                            String betResponseJson = objectMapper.writeValueAsString(BetResponse.from(savedBet));
                            betIdempotencyService.markBetAsProcessed(betId, betResponseJson);
                            log.debug("Bet idempotency key stored - betId: {}", betId);
                        } catch (Exception e) {
                            log.error("CRITICAL: Failed to store bet idempotency key - betId: {}, error: {}", betId, e.getMessage());
                            // Log error but continue - database is source of truth
                        }
                        
                        // 5. Get operator bet webhook URL
                        return operatorWebhookService.getOperatorBetWebhookUrl(operatorId)
                            .flatMap(webhookUrl -> {
                                // 7. Prepare bet request for operator
                                Map<String, Object> operatorBetRequest = buildOperatorBetRequest(
                                    betId, playerId, operatorId, request);
                                
                                // 8. Post bet to operator and wait for response
                                // CRITICAL: This must complete BEFORE game starts
                                return operatorServiceClient.postBetToOperator(webhookUrl, operatorBetRequest)
                                    .flatMap(operatorResponse -> {
                                        LocalDateTime operatorResponseAt = LocalDateTime.now();
                                        
                                        // 9. Check if game started while waiting for operator response (sync)
                                        return Mono.just(gameServiceClient.checkGameRoundState(
                                            request.getGameCode(), request.getGameRoundId()))
                                            .flatMap(currentGameState -> {
                                                // CRITICAL CHECK: If game started before operator response, auto-reject
                                                if (currentGameState.isGameStarted()) {
                                                    log.warn("CRITICAL: Game started before operator confirmation - betId: {}, auto-rejecting as NOT_CONFIRMED", betId);
                                                    
                                                    savedBet.setStatus(BetStatus.NOT_CONFIRMED);
                                                    savedBet.setOperatorResponseAt(operatorResponseAt);
                                                    savedBet.setGameStartedAt(currentGameState.getGameStartTime());
                                                    savedBet.setOperatorResponse(operatorResponse.getResponsePayload());
                                                    savedBet.setOperatorResponseMessage(
                                                        "Bet not confirmed by operator before game start. Game started at: " + 
                                                        currentGameState.getGameStartTime());
                                                    
                                                    betRepository.save(savedBet);
                                                    
                                                    return Mono.error(new BetRejectedException(
                                                        "Bet not confirmed by operator before game start. Bet rejected as NOT_CONFIRMED."));
                                                }
                                                
                                                // 10. Process operator response
                                                return processOperatorResponse(
                                                    savedBet, operatorResponse, operatorResponseAt, currentGameState);
                                            });
                                    })
                                    .onErrorResume(error -> {
                                        // Handle timeout or error posting to operator
                                        log.error("Error posting bet to operator - betId: {}", betId, error);
                                        
                                        // Check if game started during error (sync)
                                        return Mono.just(gameServiceClient.checkGameRoundState(
                                            request.getGameCode(), request.getGameRoundId()))
                                            .flatMap(currentGameState -> {
                                                if (currentGameState.isGameStarted()) {
                                                    // Game started, mark as NOT_CONFIRMED
                                                    savedBet.setStatus(BetStatus.NOT_CONFIRMED);
                                                    savedBet.setOperatorResponseMessage(
                                                        "Operator webhook error/timeout. Game started before confirmation.");
                                                    savedBet.setGameStartedAt(currentGameState.getGameStartTime());
                                                    betRepository.save(savedBet);
                                                    
                                                    return Mono.error(new BetRejectedException(
                                                        "Bet not confirmed by operator before game start. Operator webhook error: " + 
                                                        error.getMessage()));
                                                } else {
                                                    // Game hasn't started, but operator error - reject
                                                    savedBet.setStatus(BetStatus.REJECTED);
                                                    savedBet.setOperatorResponseMessage("Operator webhook error: " + error.getMessage());
                                                    betRepository.save(savedBet);
                                                    
                                                    return Mono.error(new BetRejectedException(
                                                        "Operator webhook error: " + error.getMessage()));
                                                }
                                            });
                                    });
                            });
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
     * Process operator response and update bet status accordingly
     */
    private Mono<BetResponse> processOperatorResponse(
            Bet bet, 
            OperatorServiceClient.OperatorBetResponse operatorResponse,
            LocalDateTime operatorResponseAt,
            GameServiceClient.GameRoundState gameState) {
        
        bet.setOperatorResponseAt(operatorResponseAt);
        bet.setOperatorResponse(operatorResponse.getResponsePayload());
        bet.setOperatorResponseMessage(operatorResponse.getMessage());
        
        switch (operatorResponse.getResponseType()) {
            case SUCCESS:
                // Operator confirmed bet successfully BEFORE game start
                bet.setStatus(BetStatus.ACCEPTED);
                log.info("Bet ACCEPTED - betId: {}, operator confirmed before game start", bet.getBetId());
                break;
                
            case LOW_BALANCE:
            case NO_BALANCE:
                bet.setStatus(BetStatus.REJECTED);
                bet.setOperatorResponseMessage("Operator rejected: " + operatorResponse.getMessage());
                log.warn("Bet REJECTED - betId: {}, reason: {}", bet.getBetId(), operatorResponse.getMessage());
                break;
                
            case NOT_A_PLAYER:
                bet.setStatus(BetStatus.REJECTED);
                bet.setOperatorResponseMessage("Operator rejected: Player not found");
                log.warn("Bet REJECTED - betId: {}, reason: Not a player", bet.getBetId());
                break;
                
            case REJECTED:
            case TIMEOUT:
            default:
                bet.setStatus(BetStatus.REJECTED);
                bet.setOperatorResponseMessage("Operator rejected: " + operatorResponse.getMessage());
                log.warn("Bet REJECTED - betId: {}, reason: {}", bet.getBetId(), operatorResponse.getMessage());
                break;
        }
        
        Bet savedBet = betRepository.save(bet);
        
        // Update idempotency key with final status (bet was already marked as processed earlier)
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String betResponseJson = objectMapper.writeValueAsString(BetResponse.from(savedBet));
            betIdempotencyService.markBetAsProcessed(bet.getBetId(), betResponseJson);
        } catch (Exception e) {
            log.warn("Failed to update bet idempotency key: {}", e.getMessage());
            // Continue even if idempotency storage fails
        }
        
        if (bet.getStatus() == BetStatus.ACCEPTED) {
            return Mono.just(BetResponse.from(savedBet));
        } else {
            return Mono.error(new BetRejectedException(
                "Operator rejected bet: " + bet.getOperatorResponseMessage()));
        }
    }
    
    /**
     * Build bet request payload for operator webhook
     */
    private Map<String, Object> buildOperatorBetRequest(
            String betId, Long playerId, Long operatorId, BetRequest request) {
        Map<String, Object> operatorRequest = new HashMap<>();
        operatorRequest.put("betId", betId);
        operatorRequest.put("playerId", String.valueOf(playerId));
        operatorRequest.put("operatorId", operatorId);
        operatorRequest.put("gameCode", request.getGameCode());
        operatorRequest.put("gameRoundId", request.getGameRoundId());
        operatorRequest.put("betType", request.getBetType());
        operatorRequest.put("betCategory", request.getBetCategory());
        operatorRequest.put("betAmount", request.getBetAmount());
        operatorRequest.put("currency", request.getCurrency());
        if (request.getBetDetails() != null) {
            operatorRequest.put("betDetails", request.getBetDetails());
        }
        return operatorRequest;
    }
    
    /**
     * Generate unique bet ID based on request parameters.
     * This ensures idempotency - same request generates same bet ID.
     * 
     * Format: BET_{gameCode}_{gameRoundId}_{playerId}_{hash}
     * Hash is based on: betType + betAmount + currency + betCategory
     * 
     * IMPORTANT: gameRoundId is required for idempotency.
     * Same player + same round + same bet = same bet ID (prevents duplicates).
     */
    private String generateBetId(BetRequest request, Long playerId, Long operatorId) {
        // Create a deterministic hash from request parameters for idempotency
        // Include all parameters that make a bet unique
        String requestHash = String.format("%s_%s_%s_%s_%s_%s_%d",
            request.getGameCode(),
            request.getGameRoundId(), // Required for idempotency
            request.getBetCategory(),
            request.getBetType(),
            request.getBetAmount(),
            request.getCurrency(),
            playerId
        );
        
        // Generate hash for idempotency (use first 8 chars of hex hash)
        int hashCode = requestHash.hashCode();
        String hexHash = Integer.toHexString(Math.abs(hashCode));
        String hash = hexHash.substring(0, Math.min(8, hexHash.length())).toUpperCase();
        
        // Generate bet ID: BET_{gameCode}_{gameRoundId}_{playerId}_{hash}
        return String.format("BET_%s_%s_%d_%s",
            request.getGameCode(),
            request.getGameRoundId(),
            playerId,
            hash
        );
    }
}
