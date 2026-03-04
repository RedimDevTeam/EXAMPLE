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
 * B2B Integration Refund Request DTO
 * Uses industry-standard field naming conventions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class B2BRefundRequest {
    
    @NotBlank(message = "Operator code is required")
    @Size(min = 3, max = 50, message = "Operator code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Operator code must contain only uppercase letters, numbers, and underscores")
    private String operatorCode;
    
    @NotBlank(message = "Player ID is required")
    @Size(max = 100, message = "Player ID must not exceed 100 characters")
    private String playerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO 4217 code")
    private String currency;
    
    @NotNull(message = "Unit type is required")
    private UnitType unitType;
    
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;
    
    /**
     * Original transaction ID to refund
     */
    @NotBlank(message = "Original transaction ID is required")
    @Size(max = 100, message = "Original transaction ID must not exceed 100 characters")
    private String originalTransactionId;
    
    @Size(max = 100, message = "Round ID must not exceed 100 characters")
    private String roundId;
    
    @Size(max = 100, message = "Game ID must not exceed 100 characters")
    private String gameId;
    
    @Min(value = 300, message = "Transaction subtype ID must be between 300 and 307")
    @Max(value = 307, message = "Transaction subtype ID must be between 300 and 307")
    private Integer transactionSubtypeId;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
