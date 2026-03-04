package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.MaintenanceRequest;
import com.b2bplatform.operator.dto.response.MaintenanceStatusResponse;
import com.b2bplatform.operator.model.Operator;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing operator maintenance mode.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorMaintenanceService {
    
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    /**
     * Get maintenance status for an operator.
     * Auto-disables maintenance if end time has passed.
     */
    public MaintenanceStatusResponse getMaintenanceStatus(Long operatorId) {
        log.debug("Fetching maintenance status for operator: {}", operatorId);
        
        Operator operator = operatorRepository.findById(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("Operator not found with id: " + operatorId));
        
        // Auto-disable maintenance if end time has passed
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(operator.getMaintenanceMode()) 
            && operator.getMaintenanceEndTime() != null 
            && operator.getMaintenanceEndTime().isBefore(now)) {
            log.info("Auto-disabling maintenance for operator: {} (end time passed)", operatorId);
            operator.setMaintenanceMode(false);
            operator.setMaintenanceStartTime(null);
            operator.setMaintenanceEndTime(null);
            operator.setMaintenanceMessage(null);
            operator = operatorRepository.save(operator);
        }
        
        return buildMaintenanceStatusResponse(operator);
    }
    
    /**
     * Set maintenance mode for an operator.
     */
    @Transactional
    public MaintenanceStatusResponse setMaintenanceMode(Long operatorId, MaintenanceRequest request) {
        log.info("Setting maintenance mode for operator: {}, enabled: {}", operatorId, request.getEnabled());
        
        Operator operator = operatorRepository.findById(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("Operator not found with id: " + operatorId));
        
        if (request.getEnabled() == null) {
            throw new IllegalArgumentException("Maintenance enabled status is required");
        }
        
        // Capture old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("maintenanceMode", operator.getMaintenanceMode());
        oldValues.put("maintenanceStartTime", operator.getMaintenanceStartTime());
        oldValues.put("maintenanceEndTime", operator.getMaintenanceEndTime());
        oldValues.put("maintenanceMessage", operator.getMaintenanceMessage());
        
        if (request.getEnabled()) {
            // Enable maintenance mode
            operator.setMaintenanceMode(true);
            operator.setMaintenanceStartTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now());
            operator.setMaintenanceEndTime(request.getEndTime());
            operator.setMaintenanceMessage(request.getMessage());
            
            log.info("Maintenance mode enabled for operator: {} (start: {}, end: {})", 
                operatorId, operator.getMaintenanceStartTime(), operator.getMaintenanceEndTime());
        } else {
            // Disable maintenance mode
            operator.setMaintenanceMode(false);
            operator.setMaintenanceStartTime(null);
            operator.setMaintenanceEndTime(null);
            operator.setMaintenanceMessage(null);
            
            log.info("Maintenance mode disabled for operator: {}", operatorId);
        }
        
        operator = operatorRepository.save(operator);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("maintenanceMode", operator.getMaintenanceMode());
        newValues.put("maintenanceStartTime", operator.getMaintenanceStartTime());
        newValues.put("maintenanceEndTime", operator.getMaintenanceEndTime());
        newValues.put("maintenanceMessage", operator.getMaintenanceMessage());
        
        String actionType = request.getEnabled() ? "MAINTENANCE_ENABLED" : "MAINTENANCE_DISABLED";
        auditService.logAuditEvent(
            operatorId,
            actionType,
            "Maintenance mode " + (request.getEnabled() ? "enabled" : "disabled") + " for operator: " + operator.getCode(),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return buildMaintenanceStatusResponse(operator);
    }
    
    /**
     * Check if operator is currently in maintenance (for runtime services).
     * This method may auto-disable maintenance if end time has passed.
     */
    public boolean isInMaintenance(Long operatorId) {
        Operator operator = operatorRepository.findById(operatorId).orElse(null);
        
        if (operator == null || !Boolean.TRUE.equals(operator.getMaintenanceMode())) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = operator.getMaintenanceStartTime();
        LocalDateTime endTime = operator.getMaintenanceEndTime();
        
        // If no start time set, maintenance is active
        if (startTime == null) {
            return true;
        }
        
        // If start time is in the future, maintenance hasn't started yet
        if (startTime.isAfter(now)) {
            return false;
        }
        
        // If end time is set and has passed, maintenance is over
        if (endTime != null && endTime.isBefore(now)) {
            // Auto-disable maintenance if end time has passed
            log.info("Auto-disabling maintenance for operator: {} (end time passed)", operatorId);
            operator.setMaintenanceMode(false);
            operator.setMaintenanceStartTime(null);
            operator.setMaintenanceEndTime(null);
            operator.setMaintenanceMessage(null);
            operatorRepository.save(operator);
            return false;
        }
        
        // Maintenance is active
        return true;
    }
    
    /**
     * Check if operator is currently in maintenance (read-only, doesn't modify state).
     */
    private boolean checkMaintenanceStatus(Operator operator) {
        if (operator == null || !Boolean.TRUE.equals(operator.getMaintenanceMode())) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = operator.getMaintenanceStartTime();
        LocalDateTime endTime = operator.getMaintenanceEndTime();
        
        // If no start time set, maintenance is active
        if (startTime == null) {
            return true;
        }
        
        // If start time is in the future, maintenance hasn't started yet
        if (startTime.isAfter(now)) {
            return false;
        }
        
        // If end time is set and has passed, maintenance is over
        if (endTime != null && endTime.isBefore(now)) {
            return false;
        }
        
        // Maintenance is active
        return true;
    }
    
    /**
     * Build maintenance status response from operator entity.
     */
    private MaintenanceStatusResponse buildMaintenanceStatusResponse(Operator operator) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = operator.getMaintenanceStartTime();
        LocalDateTime endTime = operator.getMaintenanceEndTime();
        
        // Determine if maintenance is currently active (read-only check)
        boolean isInMaintenance = checkMaintenanceStatus(operator);
        boolean isScheduled = false;
        Long minutesRemaining = null;
        
        // Check if maintenance is scheduled for future
        if (Boolean.TRUE.equals(operator.getMaintenanceMode()) && startTime != null && startTime.isAfter(now)) {
            isScheduled = true;
            minutesRemaining = Duration.between(now, startTime).toMinutes();
        }
        // Check if maintenance is ending soon (calculate remaining time)
        else if (isInMaintenance && endTime != null && endTime.isAfter(now)) {
            minutesRemaining = Duration.between(now, endTime).toMinutes();
        }
        
        return MaintenanceStatusResponse.builder()
            .operatorId(operator.getId())
            .isInMaintenance(isInMaintenance)
            .maintenanceStartTime(startTime)
            .maintenanceEndTime(endTime)
            .maintenanceMessage(operator.getMaintenanceMessage())
            .isScheduled(isScheduled)
            .minutesRemaining(minutesRemaining)
            .build();
    }
}
