package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateGameBetLimitRequest {
    
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
