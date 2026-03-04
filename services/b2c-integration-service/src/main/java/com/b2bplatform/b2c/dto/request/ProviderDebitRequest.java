package com.b2bplatform.b2c.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for provider wallet debit operation
 * Uses industry-standard field naming conventions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDebitRequest {
    
    @NotBlank(message = "Player ID is required")
    @Size(max = 100, message = "Player ID must not exceed 100 characters")
    private String playerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private java.math.BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code (3 uppercase letters)")
    private String currency;
    
    @NotBlank(message = "Unit type is required")
    @Pattern(regexp = "^(CENTS|DECIMAL)$", message = "Unit type must be CENTS or DECIMAL")
    private String unitType;
    
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;
    
    @NotNull(message = "Transaction subtype ID is required")
    @Min(value = 300, message = "Transaction subtype ID must be between 300 and 307")
    @Max(value = 307, message = "Transaction subtype ID must be between 300 and 307")
    private Integer transactionSubtypeId; // Industry standard (300-307)
    
    @NotNull(message = "Player level is required")
    @Min(value = 0, message = "Player level must be between 0 and 3")
    @Max(value = 3, message = "Player level must be between 0 and 3")
    private Integer playerLevel; // 0: Low, 1: Regular, 2: High, 3: VIP
    
    @NotBlank(message = "Game ID is required")
    @Size(max = 100, message = "Game ID must not exceed 100 characters")
    private String gameId; // Industry standard naming
    
    @NotBlank(message = "Round ID is required")
    @Size(max = 100, message = "Round ID must not exceed 100 characters")
    private String roundId;
    
    @Size(max = 100, message = "Hand ID must not exceed 100 characters")
    private String handId; // Optional, for card games
    
    @Size(max = 100, message = "Brand ID must not exceed 100 characters")
    private String brandId; // Optional, brand/skin identifier
    
    @Size(max = 100, message = "Agent ID must not exceed 100 characters")
    private String agentId; // Optional, agent/affiliate identifier
    
    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code (2 lowercase letters)")
    @Size(max = 10, message = "Language must not exceed 10 characters")
    private String language; // Optional, ISO 639-1 language code
}
