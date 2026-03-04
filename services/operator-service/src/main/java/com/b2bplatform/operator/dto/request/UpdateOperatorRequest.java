package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an operator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOperatorRequest {
    
    @Size(max = 200, message = "Operator name must not exceed 200 characters")
    private String name;
    
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    private String status;
    
    @Size(min = 3, max = 3, message = "Base currency must be a 3-letter ISO code")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Base currency must be uppercase 3-letter code")
    private String baseCurrency;
    
    @Size(max = 10, message = "Base language must not exceed 10 characters")
    private String baseLanguage;
    
    @Pattern(regexp = "^(LIVE|UAT|STAGING|DEMO)$", message = "Environment must be LIVE, UAT, STAGING, or DEMO")
    private String environment;
    
    @Pattern(regexp = "^(SHARED_WALLET|FUND_TRANSFER|AMS)$", message = "Integration type must be SHARED_WALLET, FUND_TRANSFER, or AMS")
    private String integrationType;
}
