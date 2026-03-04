package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateOperatorCurrencyRequest {
    
    @Size(max = 100, message = "Currency name must not exceed 100 characters")
    private String currencyName; // Can update name for custom currencies
    
    private Boolean isDefault;
    
    private Boolean isActive;
    
    @DecimalMin(value = "0.000001", message = "Exchange rate must be positive")
    @Digits(integer = 13, fraction = 6, message = "Exchange rate format is invalid")
    private BigDecimal exchangeRate;
}
