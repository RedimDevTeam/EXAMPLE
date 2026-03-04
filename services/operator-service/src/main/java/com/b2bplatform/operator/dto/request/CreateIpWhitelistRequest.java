package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating an IP whitelist entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateIpWhitelistRequest {
    
    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$|^::1$|^localhost$|^127\\.0\\.0\\.1$", 
             message = "IP address must be a valid IPv4, IPv6, localhost, or 127.0.0.1")
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;
    
    @Size(max = 50, message = "Maximum 50 endpoints allowed")
    private List<@NotBlank(message = "Endpoint cannot be blank") 
                 @Pattern(regexp = "^/api/v1/.*", message = "Endpoint must start with /api/v1/") 
                 String> allowedEndpoints; // Optional: null = all endpoints allowed
    
    @NotNull(message = "isActive is required")
    private Boolean isActive = true;
}
