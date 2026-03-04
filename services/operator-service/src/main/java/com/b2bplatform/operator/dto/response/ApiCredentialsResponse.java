package com.b2bplatform.operator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCredentialsResponse {
    
    private Long id;
    private Long operatorId;
    private String username;
    private Boolean isActive;
    private Boolean isLocked;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime expiresAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
