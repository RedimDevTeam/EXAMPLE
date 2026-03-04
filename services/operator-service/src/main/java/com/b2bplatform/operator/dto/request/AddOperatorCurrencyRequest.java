package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddOperatorCurrencyRequest {
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 1, max = 10, message = "Currency code must be between 1 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Currency code must contain only uppercase letters and numbers")
    private String currencyCode;
    
    private Boolean isCustom = false; // TRUE for custom currencies, FALSE for ISO 4217 standard
    
    @Size(max = 100, message = "Currency name must not exceed 100 characters")
    private String currencyName; // Required for custom currencies, optional for standard
    
    private Boolean isDefault = false;
    
    private BigDecimal exchangeRate; // Optional, for future use
}
