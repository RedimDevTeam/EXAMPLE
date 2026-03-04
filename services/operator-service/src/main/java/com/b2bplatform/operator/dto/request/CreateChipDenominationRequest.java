package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateChipDenominationRequest {
    
    @NotBlank(message = "Game provider ID is required")
    @Size(max = 100, message = "Game provider ID must not exceed 100 characters")
    private String gameProviderId;
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 1, max = 10, message = "Currency code must be between 1 and 10 characters")
    private String currencyCode;
    
    @NotNull(message = "Chip index is required")
    @Min(value = 0, message = "Chip index must be non-negative (0 or greater)")
    private Integer chipIndex; // Flexible: 0-based index (e.g., 0-4 for 5 chips, 0-5 for 6 chips, 0-6 for 7 chips)
    
    @NotNull(message = "Chip value is required")
    @DecimalMin(value = "0.01", message = "Chip value must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Chip value format is invalid")
    private BigDecimal chipValue;
    
    private Integer displayOrder; // Optional, defaults to chipIndex
}
