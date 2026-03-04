package com.b2bplatform.operator.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for operator operations.
 * Excludes internal fields and only exposes necessary information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatorResponse {
    
    private Long id;
    private String code;
    private String name;
    private String status;
    private String baseCurrency;
    private String baseLanguage;
    private String environment;
    private String integrationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
