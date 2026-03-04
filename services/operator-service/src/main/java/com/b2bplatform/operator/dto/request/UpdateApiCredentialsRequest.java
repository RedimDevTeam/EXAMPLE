package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateApiCredentialsRequest {
    
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password; // Optional - only update if provided
    
    private Boolean isActive;
}
