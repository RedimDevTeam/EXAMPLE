package com.b2bplatform.b2c.dto.request;

import com.b2bplatform.b2c.model.ProviderConfig.AuthType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing B2C provider configuration
 * All fields are optional - only provided fields will be updated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProviderConfigRequest {
    
    @Size(max = 200, message = "Provider name must not exceed 200 characters")
    private String providerName;
    
    @Size(max = 500, message = "API base URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.+", message = "API base URL must be a valid HTTP/HTTPS URL")
    private String apiBaseUrl;
    
    @Size(max = 255, message = "API key must not exceed 255 characters")
    private String apiKey;
    
    @Size(max = 255, message = "API secret must not exceed 255 characters")
    private String apiSecret;
    
    @Pattern(regexp = "^(API_KEY|HMAC|OAUTH)$", message = "Auth type must be API_KEY, HMAC, or OAUTH")
    private String authType;
    
    private Boolean supportsXml;
    
    private Boolean supportsJson;
    
    @Min(value = 1000, message = "Timeout must be at least 1000ms")
    @Max(value = 60000, message = "Timeout must not exceed 60000ms")
    private Integer timeoutMs;
    
    @Min(value = 0, message = "Retry attempts must be non-negative")
    @Max(value = 10, message = "Retry attempts must not exceed 10")
    private Integer retryAttempts;
    
    private Boolean isActive;
    
    /**
     * Convert auth type string to enum (if provided)
     */
    public AuthType getAuthTypeEnum() {
        if (authType == null) {
            return null;
        }
        return AuthType.valueOf(authType);
    }
}
