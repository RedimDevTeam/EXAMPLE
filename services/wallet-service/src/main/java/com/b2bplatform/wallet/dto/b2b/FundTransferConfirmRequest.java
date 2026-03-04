package com.b2bplatform.wallet.dto.b2b;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fund Transfer Confirm Request DTO (Step 2: Confirm Pending Transaction)
 * Uses industry-standard field naming conventions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferConfirmRequest {
    
    /**
     * Payment ID from Step 1 (requestFund response)
     */
    @NotBlank(message = "Payment ID is required")
    @Size(max = 100, message = "Payment ID must not exceed 100 characters")
    private String paymentId;
    
    /**
     * Transaction ID (from operator system)
     * Industry standard naming
     */
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;
    
    /**
     * Confirmation type
     * For deposit: "deposit-confirm"
     * For withdrawal: "withdrawal-confirm"
     */
    @NotBlank(message = "Type is required")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;
}
