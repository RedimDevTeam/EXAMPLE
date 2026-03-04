package com.b2bplatform.wallet.dto.b2b;

import com.b2bplatform.wallet.model.TransactionSubtype;
import com.b2bplatform.wallet.model.UnitType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * B2B Integration Debit Request DTO
 * Uses industry-standard field naming conventions
 * 
 * Industry Standard Names:
 * - operatorCode (not operatorId)
 * - transactionId (not txnId)
 * - transactionSubtypeId (not txnSubTypeId)
 * - gameId (not gameKey)
 * - brandId (not skinId)
 * - language (not lang)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class B2BDebitRequest {
    
    /**
     * Operator code/identifier (Industry standard - String, not Long)
     */
    @NotBlank(message = "Operator code is required")
    @Size(min = 3, max = 50, message = "Operator code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Operator code must contain only uppercase letters, numbers, and underscores")
    private String operatorCode;
    
    /**
     * Player identifier
     */
    @NotBlank(message = "Player ID is required")
    @Size(max = 100, message = "Player ID must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Player ID must contain only alphanumeric characters, underscores, and hyphens")
    private String playerId;
    
    /**
     * Transaction amount
     * Format depends on unitType: CENTS (long) or DECIMAL (BigDecimal)
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    /**
     * ISO 4217 currency code
     */
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO 4217 code (e.g., USD, EUR)")
    private String currency;
    
    /**
     * Unit type for amount (CENTS or DECIMAL)
     */
    @NotNull(message = "Unit type is required")
    private UnitType unitType;
    
    /**
     * Unique transaction identifier (Industry standard naming)
     */
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Transaction ID must contain only alphanumeric characters, underscores, and hyphens")
    private String transactionId;
    
    /**
     * Game round reference identifier
     */
    @Size(max = 100, message = "Round ID must not exceed 100 characters")
    private String roundId;
    
    /**
     * Game identifier (Industry standard naming - was gameKey)
     */
    @Size(max = 100, message = "Game ID must not exceed 100 characters")
    private String gameId;
    
    /**
     * Game hand identifier (for card games)
     */
    @Size(max = 100, message = "Hand ID must not exceed 100 characters")
    private String handId;
    
    /**
     * Transaction subtype code (300-307) (Industry standard naming)
     */
    @NotNull(message = "Transaction subtype ID is required")
    @Min(value = 300, message = "Transaction subtype ID must be between 300 and 307")
    @Max(value = 307, message = "Transaction subtype ID must be between 300 and 307")
    private Integer transactionSubtypeId;
    
    /**
     * Player level / Bet limit tier (0=Low, 1=Regular, 2=High, 3=VIP)
     */
    @Min(value = 0, message = "Player level must be between 0 and 3")
    @Max(value = 3, message = "Player level must be between 0 and 3")
    private Integer playerLevel;
    
    /**
     * Brand/skin identifier (Industry standard naming - was skinId)
     */
    @Size(max = 100, message = "Brand ID must not exceed 100 characters")
    private String brandId;
    
    /**
     * Site identifier (optional)
     */
    @Size(max = 100, message = "Site ID must not exceed 100 characters")
    private String siteId;
    
    /**
     * Agent system reference
     */
    @Size(max = 100, message = "Agent ID must not exceed 100 characters")
    private String agentId;
    
    /**
     * ISO 639-1 language code (Industry standard naming - was lang)
     */
    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a 2-letter ISO 639-1 code (e.g., en, es, fr)")
    @Size(min = 2, max = 2, message = "Language must be exactly 2 characters")
    private String language;
    
    /**
     * External system reference
     */
    @Size(max = 255, message = "Reference must not exceed 255 characters")
    private String reference;
    
    /**
     * Transaction description
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    /**
     * Validate transaction subtype ID
     */
    public boolean isValidTransactionSubtype() {
        return transactionSubtypeId != null && TransactionSubtype.isValidCode(transactionSubtypeId);
    }
    
    /**
     * Get TransactionSubtype enum from code
     */
    public TransactionSubtype getTransactionSubtype() {
        if (transactionSubtypeId == null) {
            return null;
        }
        try {
            return TransactionSubtype.fromCode(transactionSubtypeId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
