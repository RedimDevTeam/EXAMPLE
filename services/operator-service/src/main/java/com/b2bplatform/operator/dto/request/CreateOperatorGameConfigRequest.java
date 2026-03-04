package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateOperatorGameConfigRequest {
    
    @NotBlank(message = "Game provider ID is required")
    @Size(max = 100, message = "Game provider ID must not exceed 100 characters")
    private String gameProviderId;
    
    @NotBlank(message = "Game ID is required")
    @Size(max = 100, message = "Game ID must not exceed 100 characters")
    private String gameId;
    
    private Boolean isEnabled = true;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    @Size(max = 500, message = "Launch URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*|)$", message = "Launch URL must be a valid HTTP/HTTPS URL or empty")
    private String launchUrl;
}
