package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateChipDenominationRequest {
    
    @DecimalMin(value = "0.01", message = "Chip value must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Chip value format is invalid")
    private BigDecimal chipValue;
    
    private Boolean isActive;
    
    private Integer displayOrder;
}
