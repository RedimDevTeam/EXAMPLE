package com.b2bplatform.b2c.dto.request;

import com.b2bplatform.b2c.model.ProviderConfig.AuthType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new B2C provider configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProviderConfigRequest {
    
    @NotBlank(message = "Provider ID is required")
    @Size(min = 3, max = 100, message = "Provider ID must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Provider ID must contain only uppercase letters, numbers, and underscores")
    private String providerId;
    
    @NotBlank(message = "Provider name is required")
    @Size(max = 200, message = "Provider name must not exceed 200 characters")
    private String providerName;
    
    @NotBlank(message = "API base URL is required")
    @Size(max = 500, message = "API base URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.+", message = "API base URL must be a valid HTTP/HTTPS URL")
    private String apiBaseUrl;
    
    @NotBlank(message = "API key is required")
    @Size(max = 255, message = "API key must not exceed 255 characters")
    private String apiKey;
    
    @Size(max = 255, message = "API secret must not exceed 255 characters")
    private String apiSecret; // Required for HMAC auth
    
    @NotNull(message = "Auth type is required")
    @Pattern(regexp = "^(API_KEY|HMAC|OAUTH)$", message = "Auth type must be API_KEY, HMAC, or OAUTH")
    private String authType;
    
    @Builder.Default
    private Boolean supportsXml = false;
    
    @Builder.Default
    private Boolean supportsJson = true;
    
    @Min(value = 1000, message = "Timeout must be at least 1000ms")
    @Max(value = 60000, message = "Timeout must not exceed 60000ms")
    @Builder.Default
    private Integer timeoutMs = 5000;
    
    @Min(value = 0, message = "Retry attempts must be non-negative")
    @Max(value = 10, message = "Retry attempts must not exceed 10")
    @Builder.Default
    private Integer retryAttempts = 3;
    
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Convert auth type string to enum
     */
    public AuthType getAuthTypeEnum() {
        if (authType == null) {
            return AuthType.API_KEY;
        }
        return AuthType.valueOf(authType);
    }
}
