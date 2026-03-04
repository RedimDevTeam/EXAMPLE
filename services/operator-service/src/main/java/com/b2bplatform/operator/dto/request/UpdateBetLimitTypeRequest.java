package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateBetLimitTypeRequest {
    
    @DecimalMin(value = "0.01", message = "Minimum bet limit must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Minimum bet limit format is invalid")
    private BigDecimal minBetLimit;
    
    @DecimalMin(value = "0.01", message = "Maximum bet limit must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Maximum bet limit format is invalid")
    private BigDecimal maxBetLimit;
    
    private Boolean isActive;
}
