package com.b2bplatform.operator.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for API access log entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiAccessLogResponse {
    
    private Long id;
    private Long operatorId;
    private String endpoint;
    private String httpMethod;
    private Integer httpStatus;
    private String requestIp;
    private String userAgent;
    private String authenticatedBy;
    private String requestId;
    private Long responseTimeMs;
    private String errorMessage;
    private LocalDateTime createdAt;
}
