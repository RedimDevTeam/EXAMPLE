package com.b2bplatform.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for wallet debit operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebitResponse {
    
    private Boolean success;
    private String transactionId;
    private String status;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime timestamp;
    private String error;
    private String message;
}
