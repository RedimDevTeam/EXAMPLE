package com.b2bplatform.b2c.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for provider configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfigResponse {
    
    private Long id;
    private String providerId;
    private String providerName;
    private String apiBaseUrl;
    private String authType;
    private Boolean supportsXml;
    private Boolean supportsJson;
    private Integer timeoutMs;
    private Integer retryAttempts;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
