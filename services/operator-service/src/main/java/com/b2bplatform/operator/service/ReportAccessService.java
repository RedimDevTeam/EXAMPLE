package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateReportAccessRequest;
import com.b2bplatform.operator.dto.request.UpdateReportAccessRequest;
import com.b2bplatform.operator.dto.response.ReportAccessResponse;
import com.b2bplatform.operator.model.OperatorReportAccess;
import com.b2bplatform.operator.model.OperatorReportAccessLog;
import com.b2bplatform.operator.repository.OperatorReportAccessLogRepository;
import com.b2bplatform.operator.repository.OperatorReportAccessRepository;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for managing report access (hierarchical roles).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportAccessService {
    
    private final OperatorReportAccessRepository reportAccessRepository;
    private final OperatorReportAccessLogRepository accessLogRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    @Transactional
    public ReportAccessResponse createReportAccess(Long operatorId, CreateReportAccessRequest request) {
        log.info("Creating report access for operator {}, user {}", operatorId, request.getUserIdentifier());
        
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator ID cannot be null");
        }
        
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        if (reportAccessRepository.existsByOperatorIdAndUserIdentifier(operatorId, request.getUserIdentifier())) {
            throw new IllegalStateException("Report access already exists for operator " + operatorId + ", user " + request.getUserIdentifier());
        }
        
        OperatorReportAccess access = new OperatorReportAccess();
        access.setOperatorId(operatorId);
        access.setUserIdentifier(request.getUserIdentifier());
        access.setReportRole(request.getReportRole());
        access.setAllowedReportTypes(request.getAllowedReportTypes());
        access.setAllowedOperators(request.getAllowedOperators());
        access.setCanViewAllOperators(request.getCanViewAllOperators() != null ? request.getCanViewAllOperators() : 
            request.getReportRole().equals(com.b2bplatform.operator.model.ReportRole.GLOBAL_ADMIN));
        access.setCanExportReports(request.getCanExportReports() != null ? request.getCanExportReports() : true);
        access.setCanScheduleReports(request.getCanScheduleReports() != null ? request.getCanScheduleReports() : false);
        access.setAccessLevel(request.getAccessLevel());
        access.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorReportAccess saved = reportAccessRepository.save(access);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("userIdentifier", saved.getUserIdentifier());
        newValues.put("reportRole", saved.getReportRole());
        newValues.put("accessLevel", saved.getAccessLevel());
        
        auditService.logAuditEvent(
            operatorId,
            "REPORT_ACCESS_CREATED",
            String.format("Created report access for operator %d, user %s, role %s", 
                operatorId, request.getUserIdentifier(), request.getReportRole()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    public ReportAccessResponse getReportAccess(Long operatorId, String userIdentifier) {
        OperatorReportAccess access = reportAccessRepository
            .findByOperatorIdAndUserIdentifier(operatorId, userIdentifier)
            .orElseThrow(() -> new IllegalArgumentException(
                "Report access not found for operator " + operatorId + ", user " + userIdentifier));
        
        return mapToResponse(access);
    }
    
    public List<ReportAccessResponse> getReportAccessesByOperator(Long operatorId) {
        return reportAccessRepository.findByOperatorIdAndIsActiveTrue(operatorId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ReportAccessResponse updateReportAccess(Long operatorId, String userIdentifier, UpdateReportAccessRequest request) {
        log.info("Updating report access for operator {}, user {}", operatorId, userIdentifier);
        
        OperatorReportAccess access = reportAccessRepository
            .findByOperatorIdAndUserIdentifier(operatorId, userIdentifier)
            .orElseThrow(() -> new IllegalArgumentException(
                "Report access not found for operator " + operatorId + ", user " + userIdentifier));
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("reportRole", access.getReportRole());
        oldValues.put("accessLevel", access.getAccessLevel());
        oldValues.put("isActive", access.getIsActive());
        
        if (request.getReportRole() != null) {
            access.setReportRole(request.getReportRole());
        }
        if (request.getAllowedReportTypes() != null) {
            access.setAllowedReportTypes(request.getAllowedReportTypes());
        }
        if (request.getAllowedOperators() != null) {
            access.setAllowedOperators(request.getAllowedOperators());
        }
        if (request.getCanViewAllOperators() != null) {
            access.setCanViewAllOperators(request.getCanViewAllOperators());
        }
        if (request.getCanExportReports() != null) {
            access.setCanExportReports(request.getCanExportReports());
        }
        if (request.getCanScheduleReports() != null) {
            access.setCanScheduleReports(request.getCanScheduleReports());
        }
        if (request.getAccessLevel() != null) {
            access.setAccessLevel(request.getAccessLevel());
        }
        if (request.getIsActive() != null) {
            access.setIsActive(request.getIsActive());
        }
        access.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorReportAccess saved = reportAccessRepository.save(access);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("reportRole", saved.getReportRole());
        newValues.put("accessLevel", saved.getAccessLevel());
        newValues.put("isActive", saved.getIsActive());
        
        auditService.logAuditEvent(
            operatorId,
            "REPORT_ACCESS_UPDATED",
            String.format("Updated report access for operator %d, user %s", operatorId, userIdentifier),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    @Transactional
    public void deleteReportAccess(Long operatorId, String userIdentifier) {
        log.info("Deleting report access for operator {}, user {}", operatorId, userIdentifier);
        
        OperatorReportAccess access = reportAccessRepository
            .findByOperatorIdAndUserIdentifier(operatorId, userIdentifier)
            .orElseThrow(() -> new IllegalArgumentException(
                "Report access not found for operator " + operatorId + ", user " + userIdentifier));
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("userIdentifier", access.getUserIdentifier());
        oldValues.put("reportRole", access.getReportRole());
        
        reportAccessRepository.delete(access);
        
        auditService.logAuditEvent(
            operatorId,
            "REPORT_ACCESS_DELETED",
            String.format("Deleted report access for operator %d, user %s", operatorId, userIdentifier),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            null
        );
    }
    
    @Transactional
    public void logReportAccess(Long operatorId, String userIdentifier, String reportType, 
                                com.b2bplatform.operator.model.ReportRole reportRole,
                                OperatorReportAccessLog.AccessMethod accessMethod) {
        OperatorReportAccessLog log = new OperatorReportAccessLog();
        log.setOperatorId(operatorId);
        log.setUserIdentifier(userIdentifier);
        log.setReportType(reportType);
        log.setReportRole(reportRole);
        log.setAccessMethod(accessMethod);
        log.setIpAddress(RequestContextUtil.getClientIpAddress());
        log.setUserAgent(RequestContextUtil.getUserAgent());
        
        accessLogRepository.save(log);
    }
    
    private ReportAccessResponse mapToResponse(OperatorReportAccess access) {
        return ReportAccessResponse.builder()
            .id(access.getId())
            .operatorId(access.getOperatorId())
            .userIdentifier(access.getUserIdentifier())
            .reportRole(access.getReportRole())
            .allowedReportTypes(access.getAllowedReportTypes())
            .allowedOperators(access.getAllowedOperators())
            .canViewAllOperators(access.getCanViewAllOperators())
            .canExportReports(access.getCanExportReports())
            .canScheduleReports(access.getCanScheduleReports())
            .accessLevel(access.getAccessLevel())
            .isActive(access.getIsActive())
            .expiresAt(access.getExpiresAt())
            .createdBy(access.getCreatedBy())
            .createdAt(access.getCreatedAt())
            .updatedBy(access.getUpdatedBy())
            .updatedAt(access.getUpdatedAt())
            .build();
    }
}
