package com.b2bplatform.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for API key validation.
 * Used internally by OperatorServiceClient.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyValidationResponse {
    
    private Boolean valid;
    private Long operatorId;
    private String operatorCode;
    private String message;
}
