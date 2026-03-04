package com.b2bplatform.wallet.dto.b2b;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Fund Transfer Confirm Response DTO (Step 2 Response)
 * Returns confirmation status and balance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferConfirmResponse {
    
    /**
     * Status code (0 = success)
     */
    private Integer status;
    
    /**
     * Balance after transaction
     */
    private BigDecimal balance;
    
    /**
     * Currency
     */
    private String currency;
    
    /**
     * Unit type
     */
    private String unitType;
    
    /**
     * Transaction ID
     */
    private String transactionId;
    
    /**
     * Payment ID
     */
    private String paymentId;
    
    /**
     * Message
     */
    private String message;
}
