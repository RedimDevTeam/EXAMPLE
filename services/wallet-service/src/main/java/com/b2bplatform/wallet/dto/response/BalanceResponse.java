package com.b2bplatform.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for balance query operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceResponse {
    
    private Boolean success;
    private String playerId;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal lockedBalance;
    private String currency;
    private LocalDateTime timestamp;
    private String error;
    private String message;
}
