package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateOperatorGameBetLimitRequest {
    
    @NotBlank(message = "Game provider ID is required")
    @Size(max = 100, message = "Game provider ID must not exceed 100 characters")
    private String gameProviderId;
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;
    
    @NotNull(message = "Minimum bet is required")
    @DecimalMin(value = "0.01", message = "Minimum bet must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Minimum bet format is invalid")
    private BigDecimal minBet;
    
    @NotNull(message = "Maximum bet is required")
    @DecimalMin(value = "0.01", message = "Maximum bet must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Maximum bet format is invalid")
    private BigDecimal maxBet;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
}
