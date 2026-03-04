package com.b2bplatform.operator.dto.request;

import com.b2bplatform.operator.model.RevenueType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRevenueSharingRequest {
    
    @NotNull(message = "Parent operator ID is required")
    private Long parentOperatorId;
    
    @NotNull(message = "Revenue type is required")
    private RevenueType revenueType;
    
    @NotNull(message = "Parent share percentage is required")
    @DecimalMin(value = "0.00", message = "Parent share percentage must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Parent share percentage must be between 0 and 100")
    @Digits(integer = 3, fraction = 2, message = "Parent share percentage format is invalid")
    private BigDecimal parentSharePercentage;
    
    @NotNull(message = "Operator share percentage is required")
    @DecimalMin(value = "0.00", message = "Operator share percentage must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "Operator share percentage must be between 0 and 100")
    @Digits(integer = 3, fraction = 2, message = "Operator share percentage format is invalid")
    private BigDecimal operatorSharePercentage;
}
