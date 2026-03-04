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
public class SettlementRequest {
    @NotBlank(message = "Result is required")
    @Pattern(regexp = "WIN|LOSE|PUSH", message = "Result must be WIN, LOSE, or PUSH")
    private String result; // "WIN", "LOSE", "PUSH"
    
    @NotNull(message = "Payout amount is required")
    @DecimalMin(value = "0.00", message = "Payout amount must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Payout amount format is invalid")
    private BigDecimal payoutAmount; // Calculated by Game Service
    
    private Map<String, Object> settlementDetails; // Game-specific settlement details
}
