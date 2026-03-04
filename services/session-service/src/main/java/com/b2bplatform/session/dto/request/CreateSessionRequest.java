package com.b2bplatform.session.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    
    @NotNull(message = "Player ID is required")
    private Long playerId;
    
    @NotNull(message = "Operator ID is required")
    private Long operatorId;
    
    @NotBlank(message = "JWT token is required")
    private String jwtToken;
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;
    
    @Size(max = 500, message = "User agent must not exceed 500 characters")
    private String userAgent;
}
