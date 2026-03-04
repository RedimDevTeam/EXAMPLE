package com.b2bplatform.bet.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetRequest {
    @NotBlank(message = "Game code is required")
    private String gameCode;
    
    @NotBlank(message = "Game round ID is required")
    private String gameRoundId; // Required for idempotency
    
    @NotBlank(message = "Bet category is required")
    @Pattern(regexp = "MAIN_BET|SIDE_BET", message = "Bet category must be MAIN_BET or SIDE_BET")
    private String betCategory;
    
    @NotBlank(message = "Bet type is required")
    private String betType; // Game-specific: "PLAYER", "BANKER", "ANTE", etc.
    
    @NotNull(message = "Bet amount is required")
    @DecimalMin(value = "0.01", message = "Bet amount must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Bet amount format is invalid")
    private BigDecimal betAmount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;
    
    private Map<String, Object> betDetails; // Flexible game-specific data
}
