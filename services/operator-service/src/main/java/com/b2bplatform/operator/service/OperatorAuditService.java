package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.response.ApiAccessLogResponse;
import com.b2bplatform.operator.dto.response.AuditLogResponse;
import com.b2bplatform.operator.dto.response.LoginLogResponse;
import com.b2bplatform.operator.model.OperatorApiAccessLog;
import com.b2bplatform.operator.model.OperatorAuditLog;
import com.b2bplatform.operator.model.OperatorLoginLog;
import com.b2bplatform.operator.repository.OperatorApiAccessLogRepository;
import com.b2bplatform.operator.repository.OperatorAuditLogRepository;
import com.b2bplatform.operator.repository.OperatorLoginLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for managing audit logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorAuditService {
    
    private final OperatorAuditLogRepository auditLogRepository;
    private final OperatorApiAccessLogRepository apiAccessLogRepository;
    private final OperatorLoginLogRepository loginLogRepository;
    
    private static final ObjectMapper objectMapper;
    
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        objectMapper = mapper;
    }
    
    /**
     * Log an audit event (operator configuration changes, etc.).
     */
    @Transactional
    public void logAuditEvent(
            Long operatorId,
            String actionType,
            String actionDescription,
            String performedBy,
            String ipAddress,
            String requestId,
            Map<String, Object> oldValues,
            Map<String, Object> newValues) {
        
        try {
            OperatorAuditLog auditLog = new OperatorAuditLog();
            auditLog.setOperatorId(operatorId);
            auditLog.setActionType(actionType);
            auditLog.setActionDescription(actionDescription);
            auditLog.setPerformedBy(performedBy);
            auditLog.setIpAddress(ipAddress);
            auditLog.setRequestId(requestId);
            
            // Convert old/new values to JSON strings
            if (oldValues != null && !oldValues.isEmpty()) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            if (newValues != null && !newValues.isEmpty()) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }
            
            // Determine changed fields
            if (oldValues != null && newValues != null) {
                StringBuilder changedFields = new StringBuilder();
                for (String key : newValues.keySet()) {
                    Object oldValue = oldValues.get(key);
                    Object newValue = newValues.get(key);
                    if (oldValue == null && newValue != null || 
                        oldValue != null && !oldValue.equals(newValue)) {
                        if (changedFields.length() > 0) {
                            changedFields.append(", ");
                        }
                        changedFields.append(key);
                    }
                }
                auditLog.setChangedFields(changedFields.toString());
            }
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} for operator: {}", actionType, operatorId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
            // Don't throw exception - audit logging should not break business logic
        }
    }
    
    /**
     * Log API access.
     */
    @Transactional
    public void logApiAccess(
            Long operatorId,
            String endpoint,
            String httpMethod,
            Integer httpStatus,
            String requestIp,
            String userAgent,
            String authenticatedBy,
            String requestId,
            Long responseTimeMs,
            String errorMessage) {
        
        try {
            OperatorApiAccessLog accessLog = new OperatorApiAccessLog();
            accessLog.setOperatorId(operatorId);
            accessLog.setEndpoint(endpoint);
            accessLog.setHttpMethod(httpMethod);
            accessLog.setHttpStatus(httpStatus);
            accessLog.setRequestIp(requestIp);
            accessLog.setUserAgent(userAgent);
            accessLog.setAuthenticatedBy(authenticatedBy);
            accessLog.setRequestId(requestId);
            accessLog.setResponseTimeMs(responseTimeMs);
            accessLog.setErrorMessage(errorMessage);
            
            apiAccessLogRepository.save(accessLog);
            log.debug("API access log created: {} {} - Status: {}", httpMethod, endpoint, httpStatus);
        } catch (Exception e) {
            log.error("Failed to create API access log: {}", e.getMessage(), e);
            // Don't throw exception - audit logging should not break business logic
        }
    }
    
    /**
     * Log login attempt.
     */
    @Transactional
    public void logLogin(
            String username,
            String loginStatus,
            String ipAddress,
            String userAgent,
            String failureReason,
            String sessionId) {
        
        try {
            OperatorLoginLog loginLog = new OperatorLoginLog();
            loginLog.setUsername(username);
            loginLog.setLoginStatus(loginStatus);
            loginLog.setIpAddress(ipAddress);
            loginLog.setUserAgent(userAgent);
            loginLog.setFailureReason(failureReason);
            loginLog.setSessionId(sessionId);
            
            loginLogRepository.save(loginLog);
            log.debug("Login log created: {} - Status: {}", username, loginStatus);
        } catch (Exception e) {
            log.error("Failed to create login log: {}", e.getMessage(), e);
            // Don't throw exception - audit logging should not break business logic
        }
    }
    
    /**
     * Get audit logs for an operator.
     */
    public Page<AuditLogResponse> getAuditLogs(Long operatorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return auditLogRepository.findByOperatorIdOrderByCreatedAtDesc(operatorId, pageable)
            .map(this::toAuditLogResponse);
    }
    
    /**
     * Get audit logs by action type.
     */
    public Page<AuditLogResponse> getAuditLogsByActionType(String actionType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType, pageable)
            .map(this::toAuditLogResponse);
    }
    
    /**
     * Get API access logs for an operator.
     */
    public Page<ApiAccessLogResponse> getApiAccessLogs(Long operatorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return apiAccessLogRepository.findByOperatorIdOrderByCreatedAtDesc(operatorId, pageable)
            .map(this::toApiAccessLogResponse);
    }
    
    /**
     * Get login logs for a username.
     */
    public Page<LoginLogResponse> getLoginLogs(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return loginLogRepository.findByUsernameOrderByCreatedAtDesc(username, pageable)
            .map(this::toLoginLogResponse);
    }
    
    /**
     * Get failed login attempts for a username within time window.
     */
    public long getFailedLoginAttemptsCount(String username, LocalDateTime since) {
        return loginLogRepository.countFailedAttemptsSince(username, since);
    }
    
    /**
     * Convert OperatorAuditLog entity to AuditLogResponse DTO.
     */
    private AuditLogResponse toAuditLogResponse(OperatorAuditLog auditLog) {
        return AuditLogResponse.builder()
            .id(auditLog.getId())
            .operatorId(auditLog.getOperatorId())
            .actionType(auditLog.getActionType())
            .actionDescription(auditLog.getActionDescription())
            .performedBy(auditLog.getPerformedBy())
            .ipAddress(auditLog.getIpAddress())
            .requestId(auditLog.getRequestId())
            .oldValues(auditLog.getOldValues())
            .newValues(auditLog.getNewValues())
            .changedFields(auditLog.getChangedFields())
            .createdAt(auditLog.getCreatedAt())
            .build();
    }
    
    /**
     * Convert OperatorApiAccessLog entity to ApiAccessLogResponse DTO.
     */
    private ApiAccessLogResponse toApiAccessLogResponse(OperatorApiAccessLog accessLog) {
        return ApiAccessLogResponse.builder()
            .id(accessLog.getId())
            .operatorId(accessLog.getOperatorId())
            .endpoint(accessLog.getEndpoint())
            .httpMethod(accessLog.getHttpMethod())
            .httpStatus(accessLog.getHttpStatus())
            .requestIp(accessLog.getRequestIp())
            .userAgent(accessLog.getUserAgent())
            .authenticatedBy(accessLog.getAuthenticatedBy())
            .requestId(accessLog.getRequestId())
            .responseTimeMs(accessLog.getResponseTimeMs())
            .errorMessage(accessLog.getErrorMessage())
            .createdAt(accessLog.getCreatedAt())
            .build();
    }
    
    /**
     * Convert OperatorLoginLog entity to LoginLogResponse DTO.
     */
    private LoginLogResponse toLoginLogResponse(OperatorLoginLog loginLog) {
        return LoginLogResponse.builder()
            .id(loginLog.getId())
            .username(loginLog.getUsername())
            .loginStatus(loginLog.getLoginStatus())
            .ipAddress(loginLog.getIpAddress())
            .userAgent(loginLog.getUserAgent())
            .failureReason(loginLog.getFailureReason())
            .sessionId(loginLog.getSessionId())
            .createdAt(loginLog.getCreatedAt())
            .build();
    }
}
