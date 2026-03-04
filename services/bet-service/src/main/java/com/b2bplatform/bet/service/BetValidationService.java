package com.b2bplatform.bet.service;

import com.b2bplatform.bet.dto.BetRequest;
import com.b2bplatform.bet.exception.BetValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for validating generic bet rules (non-game-specific).
 * Game-specific validation is handled by Game Service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BetValidationService {
    
    /**
     * Validate generic bet rules (amount, currency, required fields).
     * Game-specific validation (bet type, odds, limits) is handled by Game Service.
     * 
     * @param request Bet request to validate
     * @throws BetValidationException if validation fails
     */
    public void validateGenericBetRules(BetRequest request) {
        if (request == null) {
            throw new BetValidationException("Bet request cannot be null");
        }
        
        // Validate game code
        if (request.getGameCode() == null || request.getGameCode().trim().isEmpty()) {
            throw new BetValidationException("Game code is required");
        }
        
        // Validate game round ID (required for idempotency)
        if (request.getGameRoundId() == null || request.getGameRoundId().trim().isEmpty()) {
            throw new BetValidationException("Game round ID is required");
        }
        
        // Validate bet category
        if (request.getBetCategory() == null || request.getBetCategory().trim().isEmpty()) {
            throw new BetValidationException("Bet category is required");
        }
        
        // Validate bet type
        if (request.getBetType() == null || request.getBetType().trim().isEmpty()) {
            throw new BetValidationException("Bet type is required");
        }
        
        // Validate bet amount
        if (request.getBetAmount() == null) {
            throw new BetValidationException("Bet amount is required");
        }
        
        if (request.getBetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BetValidationException("Bet amount must be greater than zero");
        }
        
        // Validate currency
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            throw new BetValidationException("Currency is required");
        }
        
        if (request.getCurrency().length() != 3) {
            throw new BetValidationException("Currency must be a 3-letter code (e.g., USD, EUR)");
        }
        
        log.debug("Generic bet validation passed - game: {}, betType: {}, amount: {}", 
            request.getGameCode(), request.getBetType(), request.getBetAmount());
    }
}
