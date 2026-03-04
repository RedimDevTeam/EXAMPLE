package com.b2bplatform.wallet.dto.b2b;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * B2B Integration Cancel Request DTO
 * Uses industry-standard field naming conventions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class B2BCancelRequest {
    
    @NotBlank(message = "Operator code is required")
    @Size(min = 3, max = 50, message = "Operator code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Operator code must contain only uppercase letters, numbers, and underscores")
    private String operatorCode;
    
    @NotBlank(message = "Player ID is required")
    @Size(max = 100, message = "Player ID must not exceed 100 characters")
    private String playerId;
    
    /**
     * Transaction ID to cancel
     */
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;
    
    /**
     * Round ID for idempotency
     */
    @Size(max = 100, message = "Round ID must not exceed 100 characters")
    private String roundId;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
