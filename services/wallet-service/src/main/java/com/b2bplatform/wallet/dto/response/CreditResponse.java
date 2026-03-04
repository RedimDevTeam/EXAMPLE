package com.b2bplatform.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for wallet credit operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditResponse {
    
    private Boolean success;
    private String transactionId;
    private String status;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime timestamp;
    private String error;
    private String message;
}
