package com.b2bplatform.wallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for validating wallet operation inputs.
 * Separates validation logic from business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletValidationService {
    
    /**
     * Validate debit request parameters.
     */
    public void validateDebitRequest(Long operatorId, String playerId, BigDecimal amount, String currency) {
        if (operatorId == null || operatorId <= 0) {
            throw new IllegalArgumentException("Invalid operator ID: " + operatorId);
        }
        
        if (playerId == null || playerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Player ID is required");
        }
        
        if (playerId.length() > 100) {
            throw new IllegalArgumentException("Player ID must not exceed 100 characters");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
        
        if (currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter ISO code");
        }
        
        log.debug("Debit request validation passed - operator: {}, player: {}, amount: {}", 
            operatorId, playerId, amount);
    }
    
    /**
     * Validate credit request parameters.
     */
    public void validateCreditRequest(Long operatorId, String playerId, BigDecimal amount, String currency) {
        validateDebitRequest(operatorId, playerId, amount, currency); // Same validation rules
        log.debug("Credit request validation passed - operator: {}, player: {}, amount: {}", 
            operatorId, playerId, amount);
    }
    
    /**
     * Validate balance query parameters.
     */
    public void validateBalanceQuery(Long operatorId, String playerId) {
        if (operatorId == null || operatorId <= 0) {
            throw new IllegalArgumentException("Invalid operator ID: " + operatorId);
        }
        
        if (playerId == null || playerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Player ID is required");
        }
        
        log.debug("Balance query validation passed - operator: {}, player: {}", operatorId, playerId);
    }
}
