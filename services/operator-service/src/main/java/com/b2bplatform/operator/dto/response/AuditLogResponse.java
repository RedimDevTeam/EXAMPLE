package com.b2bplatform.operator.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for operator audit log entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponse {
    
    private Long id;
    private Long operatorId;
    private String actionType;
    private String actionDescription;
    private String performedBy;
    private String ipAddress;
    private String requestId;
    private String oldValues;
    private String newValues;
    private String changedFields;
    private LocalDateTime createdAt;
}
