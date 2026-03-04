package com.b2bplatform.wallet.util;

import com.b2bplatform.wallet.model.UnifiedErrorCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Error Code Mapper
 * Maps internal exceptions and error conditions to unified error codes
 * 
 * Unified Error Codes (per B2B/B2C Unified Gaming Integration Standard v1.1):
 * - 1000: Success
 * - 2001: Insufficient Funds
 * - 2002: Account Blocked
 * - 3001: Duplicate Reference
 * - 4001: Token Invalid
 * - 5000: System Error
 */
@Slf4j
public class ErrorCodeMapper {
    
    /**
     * Map exception to unified error code
     * 
     * @param exception Exception to map
     * @return UnifiedErrorCode
     */
    public static UnifiedErrorCode mapException(Exception exception) {
        if (exception == null) {
            return UnifiedErrorCode.SYSTEM_ERROR;
        }
        
        String message = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
        String className = exception.getClass().getSimpleName();
        
        // Check for insufficient funds
        if (message.contains("insufficient") || message.contains("balance") && message.contains("low")) {
            return UnifiedErrorCode.INSUFFICIENT_FUNDS;
        }
        
        // Check for account blocked
        if (message.contains("blocked") || message.contains("block")) {
            return UnifiedErrorCode.ACCOUNT_BLOCKED;
        }
        
        // Check for duplicate reference
        if (message.contains("duplicate") || message.contains("already exists") || 
            message.contains("idempotency") || className.contains("Duplicate")) {
            return UnifiedErrorCode.DUPLICATE_REFERENCE;
        }
        
        // Check for invalid token/authentication
        if (message.contains("invalid") && (message.contains("token") || message.contains("auth") || 
            message.contains("unauthorized")) || className.contains("Authentication")) {
            return UnifiedErrorCode.TOKEN_INVALID;
        }
        
        // Check for illegal argument (validation errors)
        if (className.contains("IllegalArgument") || className.contains("Validation")) {
            // Could be various validation errors, default to system error
            return UnifiedErrorCode.SYSTEM_ERROR;
        }
        
        // Default to system error
        log.debug("Mapping exception {} to SYSTEM_ERROR", className);
        return UnifiedErrorCode.SYSTEM_ERROR;
    }
    
    /**
     * Map error message to unified error code
     * 
     * @param errorMessage Error message
     * @return UnifiedErrorCode
     */
    public static UnifiedErrorCode mapErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return UnifiedErrorCode.SYSTEM_ERROR;
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        
        // Check for insufficient funds
        if (lowerMessage.contains("insufficient") || 
            (lowerMessage.contains("balance") && lowerMessage.contains("low")) ||
            lowerMessage.contains("not enough")) {
            return UnifiedErrorCode.INSUFFICIENT_FUNDS;
        }
        
        // Check for account blocked
        if (lowerMessage.contains("blocked") || lowerMessage.contains("block") ||
            lowerMessage.contains("suspended")) {
            return UnifiedErrorCode.ACCOUNT_BLOCKED;
        }
        
        // Check for duplicate reference
        if (lowerMessage.contains("duplicate") || lowerMessage.contains("already exists") ||
            lowerMessage.contains("already processed")) {
            return UnifiedErrorCode.DUPLICATE_REFERENCE;
        }
        
        // Check for invalid token/authentication
        if (lowerMessage.contains("invalid") && (lowerMessage.contains("token") || 
            lowerMessage.contains("auth") || lowerMessage.contains("unauthorized"))) {
            return UnifiedErrorCode.TOKEN_INVALID;
        }
        
        // Default to system error
        return UnifiedErrorCode.SYSTEM_ERROR;
    }
    
    /**
     * Map specific error conditions to unified error codes
     */
    public static UnifiedErrorCode mapErrorCondition(String condition) {
        if (condition == null || condition.isEmpty()) {
            return UnifiedErrorCode.SYSTEM_ERROR;
        }
        
        return switch (condition.toUpperCase()) {
            case "INSUFFICIENT_FUNDS", "LOW_BALANCE" -> UnifiedErrorCode.INSUFFICIENT_FUNDS;
            case "ACCOUNT_BLOCKED", "BLOCKED" -> UnifiedErrorCode.ACCOUNT_BLOCKED;
            case "DUPLICATE", "DUPLICATE_REFERENCE", "IDEMPOTENCY" -> UnifiedErrorCode.DUPLICATE_REFERENCE;
            case "INVALID_TOKEN", "AUTH_FAILED", "UNAUTHORIZED" -> UnifiedErrorCode.TOKEN_INVALID;
            default -> UnifiedErrorCode.SYSTEM_ERROR;
        };
    }
}
