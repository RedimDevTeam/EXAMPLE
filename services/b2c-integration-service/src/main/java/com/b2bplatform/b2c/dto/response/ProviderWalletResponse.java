package com.b2bplatform.b2c.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for provider wallet operations (debit/credit/refund/cancel)
 * Uses unified error codes (1000 = Success)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderWalletResponse {
    
    /**
     * Unified error code (per B2B/B2C Unified Gaming Integration Standard)
     * 1000 = Success
     * 2001 = Insufficient Funds
     * 2002 = Account Blocked
     * 3001 = Duplicate Reference
     * 4001 = Token Invalid
     * 5000 = System Error
     */
    @Builder.Default
    private Integer status = 1000; // 1000 = Success
    
    private BigDecimal balance;
    private String currency;
    private String unitType; // CENTS or DECIMAL
    private String message;
    private String transactionId;
}
