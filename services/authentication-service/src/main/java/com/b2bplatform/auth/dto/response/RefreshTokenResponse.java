package com.b2bplatform.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for token refresh operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenResponse {
    
    private Boolean success;
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private LocalDateTime timestamp;
    private String error;
    private String message;
}
