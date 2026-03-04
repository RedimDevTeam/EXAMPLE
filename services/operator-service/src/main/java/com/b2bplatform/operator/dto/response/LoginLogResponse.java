package com.b2bplatform.operator.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for login log entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginLogResponse {
    
    private Long id;
    private String username;
    private String loginStatus;
    private String ipAddress;
    private String userAgent;
    private String failureReason;
    private String sessionId;
    private LocalDateTime createdAt;
}
