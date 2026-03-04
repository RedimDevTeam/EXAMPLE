package com.b2bplatform.b2c.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for provider balance query
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderBalanceResponse {
    
    /**
     * Unified error code (1000 = Success)
     */
    @Builder.Default
    private Integer status = 1000;
    
    private String playerId;
    private BigDecimal balance;
    private String currency;
    private String unitType; // CENTS or DECIMAL
    private String message;
}
