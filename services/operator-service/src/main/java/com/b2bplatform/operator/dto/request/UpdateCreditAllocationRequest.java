package com.b2bplatform.operator.dto.request;

import com.b2bplatform.operator.model.AllocationType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCreditAllocationRequest {
    
    @DecimalMin(value = "0.01", message = "Credit limit must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Credit limit format is invalid")
    private BigDecimal creditLimit;
    
    private AllocationType allocationType;
    
    private Boolean autoReplenish;
    
    @DecimalMin(value = "0.00", message = "Replenish threshold must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Replenish threshold must be between 0 and 100")
    @Digits(integer = 3, fraction = 2, message = "Replenish threshold format is invalid")
    private BigDecimal replenishThreshold;
    
    private Boolean isActive;
}
