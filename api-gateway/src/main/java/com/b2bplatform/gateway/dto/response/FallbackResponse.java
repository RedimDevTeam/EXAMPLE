package com.b2bplatform.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for circuit breaker fallback scenarios.
 * Used when downstream services are unavailable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FallbackResponse {
    
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String service; // Name of the unavailable service
    private String path;
}
