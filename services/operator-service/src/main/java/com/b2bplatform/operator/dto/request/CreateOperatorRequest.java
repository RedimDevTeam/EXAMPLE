package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new operator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOperatorRequest {
    
    @NotBlank(message = "Operator code is required")
    @Size(max = 50, message = "Operator code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Operator code must contain only uppercase letters, numbers, and underscores")
    private String code;
    
    @NotBlank(message = "Operator name is required")
    @Size(max = 200, message = "Operator name must not exceed 200 characters")
    private String name;
    
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    private String status = "ACTIVE";
    
    @Size(min = 3, max = 3, message = "Base currency must be a 3-letter ISO code")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Base currency must be uppercase 3-letter code")
    private String baseCurrency = "USD";
    
    @Size(max = 10, message = "Base language must not exceed 10 characters")
    private String baseLanguage = "en";
    
    @Pattern(regexp = "^(LIVE|UAT|STAGING|DEMO)$", message = "Environment must be LIVE, UAT, STAGING, or DEMO")
    private String environment;
    
    @Pattern(regexp = "^(SHARED_WALLET|FUND_TRANSFER|AMS)$", message = "Integration type must be SHARED_WALLET, FUND_TRANSFER, or AMS")
    private String integrationType;
}
