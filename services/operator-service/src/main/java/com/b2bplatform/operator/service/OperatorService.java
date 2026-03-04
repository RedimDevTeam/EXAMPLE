package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateOperatorRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorRequest;
import com.b2bplatform.operator.dto.response.OperatorResponse;
import com.b2bplatform.operator.model.Operator;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorService {
    
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    public List<OperatorResponse> getAllOperators() {
        log.debug("Fetching all operators");
        return operatorRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    public Optional<OperatorResponse> getOperatorById(Long id) {
        log.debug("Fetching operator with id: {}", id);
        return operatorRepository.findById(id)
            .map(this::toResponse);
    }
    
    public Optional<OperatorResponse> getOperatorByCode(String code) {
        log.debug("Fetching operator with code: {}", code);
        return operatorRepository.findByCode(code)
            .map(this::toResponse);
    }
    
    @Transactional
    public OperatorResponse createOperator(CreateOperatorRequest request) {
        log.info("Creating operator with code: {}", request.getCode());
        
        if (operatorRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Operator with code '" + request.getCode() + "' already exists");
        }
        
        Operator operator = new Operator();
        operator.setCode(request.getCode());
        operator.setName(request.getName());
        operator.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        operator.setBaseCurrency(request.getBaseCurrency() != null ? request.getBaseCurrency() : "USD");
        operator.setBaseLanguage(request.getBaseLanguage() != null ? request.getBaseLanguage() : "en");
        operator.setEnvironment(request.getEnvironment());
        operator.setIntegrationType(request.getIntegrationType());
        
        Operator saved = operatorRepository.save(operator);
        log.info("Operator created successfully with id: {}", saved.getId());
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("code", saved.getCode());
        newValues.put("name", saved.getName());
        newValues.put("status", saved.getStatus());
        newValues.put("baseCurrency", saved.getBaseCurrency());
        newValues.put("baseLanguage", saved.getBaseLanguage());
        newValues.put("environment", saved.getEnvironment());
        newValues.put("integrationType", saved.getIntegrationType());
        
        auditService.logAuditEvent(
            saved.getId(),
            "OPERATOR_CREATED",
            "Operator created: " + saved.getCode(),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return toResponse(saved);
    }
    
    @Transactional
    public OperatorResponse updateOperator(Long id, UpdateOperatorRequest request) {
        log.info("Updating operator with id: {}", id);
        
        Operator operator = operatorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Operator not found with id: " + id));
        
        // Capture old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("name", operator.getName());
        oldValues.put("status", operator.getStatus());
        oldValues.put("baseCurrency", operator.getBaseCurrency());
        oldValues.put("baseLanguage", operator.getBaseLanguage());
        oldValues.put("environment", operator.getEnvironment());
        oldValues.put("integrationType", operator.getIntegrationType());
        
        if (request.getName() != null) {
            operator.setName(request.getName());
        }
        if (request.getStatus() != null) {
            operator.setStatus(request.getStatus());
        }
        if (request.getBaseCurrency() != null) {
            operator.setBaseCurrency(request.getBaseCurrency());
        }
        if (request.getBaseLanguage() != null) {
            operator.setBaseLanguage(request.getBaseLanguage());
        }
        if (request.getEnvironment() != null) {
            operator.setEnvironment(request.getEnvironment());
        }
        if (request.getIntegrationType() != null) {
            operator.setIntegrationType(request.getIntegrationType());
        }
        
        Operator updated = operatorRepository.save(operator);
        log.info("Operator updated successfully with id: {}", updated.getId());
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("name", updated.getName());
        newValues.put("status", updated.getStatus());
        newValues.put("baseCurrency", updated.getBaseCurrency());
        newValues.put("baseLanguage", updated.getBaseLanguage());
        newValues.put("environment", updated.getEnvironment());
        newValues.put("integrationType", updated.getIntegrationType());
        
        auditService.logAuditEvent(
            id,
            "OPERATOR_UPDATED",
            "Operator updated: " + updated.getCode(),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return toResponse(updated);
    }
    
    /**
     * Convert Operator entity to OperatorResponse DTO
     */
    private OperatorResponse toResponse(Operator operator) {
        return OperatorResponse.builder()
            .id(operator.getId())
            .code(operator.getCode())
            .name(operator.getName())
            .status(operator.getStatus())
            .baseCurrency(operator.getBaseCurrency())
            .baseLanguage(operator.getBaseLanguage())
            .environment(operator.getEnvironment())
            .integrationType(operator.getIntegrationType())
            .createdAt(operator.getCreatedAt())
            .updatedAt(operator.getUpdatedAt())
            .build();
    }
    
    @Transactional
    public void deleteOperator(Long id) {
        log.info("Deleting operator with id: {}", id);
        
        Operator operator = operatorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Operator not found with id: " + id));
        
        String operatorCode = operator.getCode();
        operatorRepository.deleteById(id);
        log.info("Operator deleted successfully with id: {}", id);
        
        // Log audit event
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("code", operatorCode);
        oldValues.put("name", operator.getName());
        
        auditService.logAuditEvent(
            id,
            "OPERATOR_DELETED",
            "Operator deleted: " + operatorCode,
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            null
        );
    }
}
