package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating an IP whitelist entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIpWhitelistRequest {
    
    @Size(max = 50, message = "Maximum 50 endpoints allowed")
    private List<@Pattern(regexp = "^/api/v1/.*", message = "Endpoint must start with /api/v1/") 
                 String> allowedEndpoints; // null = all endpoints allowed
    
    private Boolean isActive;
}
