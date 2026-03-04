package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateOperatorGameConfigRequest {
    
    private Boolean isEnabled;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    @Size(max = 500, message = "Launch URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*|)$", message = "Launch URL must be a valid HTTP/HTTPS URL or empty")
    private String launchUrl;
}
