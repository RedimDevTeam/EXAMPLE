package com.b2bplatform.wallet.dto.b2b;

import com.b2bplatform.wallet.model.PendingFundTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fund Transfer Status Response DTO
 * Returns status of pending fund transfer transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferStatusResponse {
    
    /**
     * Payment ID
     */
    private String paymentId;
    
    /**
     * Status (PENDING, CONFIRMED, EXPIRED, CANCELLED)
     */
    private PendingFundTransaction.PendingStatus status;
    
    /**
     * Amount
     */
    private BigDecimal amount;
    
    /**
     * Currency
     */
    private String currency;
    
    /**
     * Unit type
     */
    private String unitType;
    
    /**
     * Transaction type (deposit or withdrawal)
     */
    private String transactionType;
    
    /**
     * Expiration timestamp
     */
    private LocalDateTime expiresAt;
    
    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Confirmation timestamp (if confirmed)
     */
    private LocalDateTime confirmedAt;
    
    /**
     * Confirmed by (if confirmed)
     */
    private String confirmedBy;
    
    /**
     * Operator response (if available)
     */
    private Object operatorResponse;
}
