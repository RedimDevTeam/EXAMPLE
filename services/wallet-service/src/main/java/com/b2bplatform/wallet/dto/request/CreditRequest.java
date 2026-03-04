package com.b2bplatform.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for wallet credit operation (win payout).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequest {
    
    @NotNull(message = "Operator ID is required")
    private Long operatorId;
    
    @NotBlank(message = "Player ID is required")
    @Size(max = 100, message = "Player ID must not exceed 100 characters")
    private String playerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code (e.g., USD, EUR)")
    private String currency = "USD";
    
    @Size(max = 255, message = "Reference must not exceed 255 characters")
    private String reference;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
