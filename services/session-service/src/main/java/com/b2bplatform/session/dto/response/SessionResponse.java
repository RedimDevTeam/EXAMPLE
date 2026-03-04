package com.b2bplatform.session.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for session operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponse {
    
    private String sessionId;
    private Long playerId;
    private Long operatorId;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private String error;
    private String message;
}
