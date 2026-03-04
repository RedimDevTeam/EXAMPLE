package com.b2bplatform.wallet.dto.b2b;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Fund Transfer Request Response DTO (Step 1 Response)
 * Returns payment ID and expiration time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferRequestResponse {
    
    /**
     * Payment ID (to be used in Step 2: confirm)
     */
    private String paymentId;
    
    /**
     * Status (should be PENDING)
     */
    private String status;
    
    /**
     * Expiration timestamp
     */
    private LocalDateTime expiresAt;
    
    /**
     * Message
     */
    private String message;
}
