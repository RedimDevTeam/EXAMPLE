package com.b2bplatform.wallet.dto.b2b;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Operational API Response DTO
 * Standard response for block/unblock/kickout operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationalResponse {
    
    /**
     * Status code (0 = success, non-zero = error)
     */
    private Integer status;
    
    /**
     * Message
     */
    private String message;
    
    /**
     * Operator code
     */
    private String operatorCode;
    
    /**
     * Player ID
     */
    private String playerId;
    
    /**
     * Is blocked (for block/unblock operations)
     */
    private Boolean isBlocked;
    
    /**
     * Kicked out timestamp (for kickout operation)
     */
    private String kickedOutAt;
}
