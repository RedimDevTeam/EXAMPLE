package com.b2bplatform.operator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for effective bet limits (used by Bet Service).
 * Contains resolved bet limits with source information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EffectiveBetLimitResponse {
    
    private Long operatorId;
    private String gameId;
    private String currencyCode;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private String source; // "OPERATOR_SPECIFIC", "GAME_SPECIFIC", or "SYSTEM_DEFAULT"
    private Long limitId; // ID of the limit used (null for SYSTEM_DEFAULT)
    private Boolean isActive;
}
