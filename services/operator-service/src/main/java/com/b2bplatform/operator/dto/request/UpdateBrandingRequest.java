package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateBrandingRequest {
    
    private Boolean isActive;
    
    @Min(value = 0, message = "Display order must be non-negative")
    private Integer displayOrder;
}
