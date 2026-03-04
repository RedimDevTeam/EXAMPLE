package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateRevenueSharingRequest {
    
    @DecimalMin(value = "0.00", message = "Parent share percentage must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Parent share percentage must be between 0 and 100")
    @Digits(integer = 3, fraction = 2, message = "Parent share percentage format is invalid")
    private BigDecimal parentSharePercentage;
    
    @DecimalMin(value = "0.00", message = "Operator share percentage must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Operator share percentage must be between 0 and 100")
    @Digits(integer = 3, fraction = 2, message = "Operator share percentage format is invalid")
    private BigDecimal operatorSharePercentage;
    
    private LocalDateTime effectiveTo;
    
    private Boolean isActive;
}
