package com.b2bplatform.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user info (token validation).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoResponse {
    
    private Boolean valid;
    private Long playerId;
    private String username;
    private String operatorCode;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String error;
    private String message;
}
