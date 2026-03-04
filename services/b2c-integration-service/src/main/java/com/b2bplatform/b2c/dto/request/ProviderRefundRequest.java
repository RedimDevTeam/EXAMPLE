package com.b2bplatform.b2c.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for provider wallet refund operation
 * Uses industry-standard field naming conventions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRefundRequest {
    
    @NotBlank(message = "Player ID is required")
    @Size(max = 100, message = "Player ID must not exceed 100 characters")
    private String playerId;
    
    @NotBlank(message = "Original transaction ID is required")
    @Size(max = 100, message = "Original transaction ID must not exceed 100 characters")
    private String originalTransactionId; // Transaction being refunded
    
    @NotBlank(message = "Refund transaction ID is required")
    @Size(max = 100, message = "Refund transaction ID must not exceed 100 characters")
    private String transactionId; // New transaction ID for refund
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private java.math.BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code (3 uppercase letters)")
    private String currency;
    
    @NotBlank(message = "Unit type is required")
    @Pattern(regexp = "^(CENTS|DECIMAL)$", message = "Unit type must be CENTS or DECIMAL")
    private String unitType;
    
    @NotNull(message = "Transaction subtype ID is required")
    @Min(value = 300, message = "Transaction subtype ID must be between 300 and 307")
    @Max(value = 307, message = "Transaction subtype ID must be between 300 and 307")
    private Integer transactionSubtypeId; // Should be 304 (REFUND)
    
    @NotBlank(message = "Round ID is required")
    @Size(max = 100, message = "Round ID must not exceed 100 characters")
    private String roundId;
    
    @NotBlank(message = "Game ID is required")
    @Size(max = 100, message = "Game ID must not exceed 100 characters")
    private String gameId;
}
