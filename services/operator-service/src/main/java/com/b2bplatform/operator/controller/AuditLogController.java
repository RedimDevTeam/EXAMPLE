package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.response.ApiAccessLogResponse;
import com.b2bplatform.operator.dto.response.AuditLogResponse;
import com.b2bplatform.operator.service.OperatorAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for viewing audit logs.
 */
@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "APIs for viewing audit logs")
public class AuditLogController {
    
    private final OperatorAuditService auditService;
    
    @GetMapping("/operators/{operatorId}")
    @Operation(summary = "Get audit logs for operator", 
               description = "Retrieve audit logs for a specific operator")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @PathVariable Long operatorId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v1/admin/audit/operators/{}", operatorId);
        
        Page<AuditLogResponse> logs = auditService.getAuditLogs(operatorId, page, size);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/actions/{actionType}")
    @Operation(summary = "Get audit logs by action type", 
               description = "Retrieve audit logs filtered by action type")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByActionType(
            @PathVariable String actionType,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v1/admin/audit/actions/{}", actionType);
        
        Page<AuditLogResponse> logs = auditService.getAuditLogsByActionType(actionType, page, size);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/api-access/operators/{operatorId}")
    @Operation(summary = "Get API access logs for operator", 
               description = "Retrieve API access logs for a specific operator")
    public ResponseEntity<Page<ApiAccessLogResponse>> getApiAccessLogs(
            @PathVariable Long operatorId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v1/admin/audit/api-access/operators/{}", operatorId);
        
        Page<ApiAccessLogResponse> logs = auditService.getApiAccessLogs(operatorId, page, size);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/login/{username}")
    @Operation(summary = "Get login logs for username", 
               description = "Retrieve login logs for a specific username")
    public ResponseEntity<Page<com.b2bplatform.operator.dto.response.LoginLogResponse>> getLoginLogs(
            @PathVariable String username,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v1/admin/audit/login/{}", username);
        
        Page<com.b2bplatform.operator.dto.response.LoginLogResponse> logs = auditService.getLoginLogs(username, page, size);
        return ResponseEntity.ok(logs);
    }
}
