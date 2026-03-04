package com.b2bplatform.b2c.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for getting provider wallet balance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderBalanceRequest {
    
    @NotBlank(message = "Player ID is required")
    @Size(max = 100, message = "Player ID must not exceed 100 characters")
    private String playerId;
    
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code (3 uppercase letters)")
    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency; // Optional, defaults to provider default currency
}
