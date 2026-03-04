package com.b2bplatform.wallet.dto.b2b;

import com.b2bplatform.wallet.model.UnitType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Fund Transfer Request DTO (Step 1: Create Pending Transaction)
 * Uses industry-standard field naming conventions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferRequest {
    
    /**
     * Operator code/identifier (Industry standard - String)
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
    private String playerId;
    
    /**
     * Transaction amount
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    /**
     * Currency code
     */
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO 4217 code")
    private String currency;
    
    /**
     * Unit type (CENTS or DECIMAL)
     */
    @NotNull(message = "Unit type is required")
    private UnitType unitType;
    
    /**
     * Transaction type (deposit or withdrawal)
     */
    @NotNull(message = "Transaction type is required")
    private FundTransferType transactionType;
    
    /**
     * Optional expiration minutes (default: 15 minutes)
     */
    @Min(value = 1, message = "Expiration minutes must be at least 1")
    @Max(value = 1440, message = "Expiration minutes must not exceed 1440 (24 hours)")
    private Integer expirationMinutes;
    
    /**
     * Fund Transfer Type
     */
    public enum FundTransferType {
        DEPOSIT,      // Player deposits funds from operator
        WITHDRAWAL    // Player withdraws funds to operator
    }
}
