package com.b2bplatform.operator.dto.request;

import com.b2bplatform.operator.model.AllocationType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCreditAllocationRequest {
    
    @NotNull(message = "Child operator ID is required")
    private Long childOperatorId;
    
    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "0.01", message = "Credit limit must be at least 0.01")
    @Digits(integer = 17, fraction = 2, message = "Credit limit format is invalid")
    private BigDecimal creditLimit;
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 1, max = 10, message = "Currency code must be between 1 and 10 characters")
    private String currencyCode;
    
    private AllocationType allocationType = AllocationType.MANUAL;
    
    private Boolean autoReplenish = false;
    
    @DecimalMin(value = "0.00", message = "Replenish threshold must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Replenish threshold must be between 0 and 100")
    @Digits(integer = 3, fraction = 2, message = "Replenish threshold format is invalid")
    private BigDecimal replenishThreshold;
}
