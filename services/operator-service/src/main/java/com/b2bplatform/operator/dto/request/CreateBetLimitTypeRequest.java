package com.b2bplatform.operator.dto.request;

import com.b2bplatform.operator.model.BetLimitType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBetLimitTypeRequest {
    
    @NotBlank(message = "Game provider ID is required")
    @Size(max = 100, message = "Game provider ID must not exceed 100 characters")
    private String gameProviderId;
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 1, max = 10, message = "Currency code must be between 1 and 10 characters")
    private String currencyCode;
    
    @NotNull(message = "Limit type is required")
    private BetLimitType limitType;
    
    @NotNull(message = "Minimum bet limit is required")
    @DecimalMin(value = "0.01", message = "Minimum bet limit must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Minimum bet limit format is invalid")
    private BigDecimal minBetLimit;
    
    @NotNull(message = "Maximum bet limit is required")
    @DecimalMin(value = "0.01", message = "Maximum bet limit must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Maximum bet limit format is invalid")
    private BigDecimal maxBetLimit;
}
