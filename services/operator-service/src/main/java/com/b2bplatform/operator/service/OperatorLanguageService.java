package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.AddOperatorLanguageRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorLanguageRequest;
import com.b2bplatform.operator.dto.response.OperatorLanguageResponse;
import com.b2bplatform.operator.model.OperatorLanguage;
import com.b2bplatform.operator.repository.OperatorLanguageRepository;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing operator languages (multi-language support).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorLanguageService {
    
    private final OperatorLanguageRepository operatorLanguageRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    /**
     * Add a language to an operator.
     */
    @Transactional
    public OperatorLanguageResponse addLanguage(Long operatorId, AddOperatorLanguageRequest request) {
        log.info("Adding language {} to operator {}", request.getLanguageCode(), operatorId);
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate language code format based on type
        String languageCodeLower = request.getLanguageCode().toLowerCase();
        boolean isCustom = Boolean.TRUE.equals(request.getIsCustom());
        
        if (isCustom) {
            // Custom language: 1-10 alphanumeric characters
            if (languageCodeLower.length() < 1 || languageCodeLower.length() > 10) {
                throw new IllegalArgumentException("Custom language code must be between 1 and 10 characters");
            }
            if (!languageCodeLower.matches("^[a-z0-9]+$")) {
                throw new IllegalArgumentException("Custom language code must contain only lowercase letters and numbers");
            }
            // Custom languages require a name
            if (request.getLanguageName() == null || request.getLanguageName().trim().isEmpty()) {
                throw new IllegalArgumentException("Language name is required for custom languages");
            }
        } else {
            // Standard ISO 639-1: exactly 2 lowercase letters
            if (languageCodeLower.length() != 2) {
                throw new IllegalArgumentException("Standard language code must be exactly 2 characters (ISO 639-1)");
            }
            if (!languageCodeLower.matches("^[a-z]{2}$")) {
                throw new IllegalArgumentException("Standard language code must contain only lowercase letters");
            }
        }
        
        // Check if language already exists for this operator
        if (operatorLanguageRepository.existsByOperatorIdAndLanguageCode(operatorId, languageCodeLower)) {
            throw new IllegalStateException(
                String.format("Language %s already exists for operator %d", languageCodeLower, operatorId));
        }
        
        // If setting as default, unset other default languages
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            operatorLanguageRepository.findByOperatorIdAndIsDefaultTrue(operatorId)
                .ifPresent(existingDefault -> {
                    existingDefault.setIsDefault(false);
                    existingDefault.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
                    operatorLanguageRepository.save(existingDefault);
                });
        }
        
        OperatorLanguage language = new OperatorLanguage();
        language.setOperatorId(operatorId);
        language.setLanguageCode(languageCodeLower);
        language.setIsCustom(isCustom);
        language.setLanguageName(request.getLanguageName());
        language.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        language.setIsActive(true);
        language.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorLanguage saved = operatorLanguageRepository.save(language);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("languageCode", saved.getLanguageCode());
        newValues.put("isCustom", saved.getIsCustom());
        newValues.put("languageName", saved.getLanguageName());
        newValues.put("isDefault", saved.getIsDefault());
        newValues.put("isActive", saved.getIsActive());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_LANGUAGE_ADDED",
            String.format("Added language %s to operator %d", request.getLanguageCode(), operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Get all languages for an operator.
     */
    public List<OperatorLanguageResponse> getLanguages(Long operatorId, Boolean activeOnly) {
        log.info("Getting languages for operator {}, activeOnly: {}", operatorId, activeOnly);
        
        List<OperatorLanguage> languages;
        if (Boolean.TRUE.equals(activeOnly)) {
            languages = operatorLanguageRepository.findByOperatorIdAndIsActiveTrueOrderByIsDefaultDescLanguageCodeAsc(operatorId);
        } else {
            languages = operatorLanguageRepository.findByOperatorIdOrderByIsDefaultDescLanguageCodeAsc(operatorId);
        }
        
        return languages.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get a specific language for an operator.
     */
    public OperatorLanguageResponse getLanguage(Long operatorId, String languageCode) {
        log.info("Getting language {} for operator {}", languageCode, operatorId);
        
        OperatorLanguage language = operatorLanguageRepository.findByOperatorIdAndLanguageCode(operatorId, languageCode.toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Language %s not found for operator %d", languageCode, operatorId)));
        
        return mapToResponse(language);
    }
    
    /**
     * Update a language for an operator.
     */
    @Transactional
    public OperatorLanguageResponse updateLanguage(Long operatorId, String languageCode, UpdateOperatorLanguageRequest request) {
        log.info("Updating language {} for operator {}", languageCode, operatorId);
        
        OperatorLanguage language = operatorLanguageRepository.findByOperatorIdAndLanguageCode(operatorId, languageCode.toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Language %s not found for operator %d", languageCode, operatorId)));
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("languageName", language.getLanguageName());
        oldValues.put("isDefault", language.getIsDefault());
        oldValues.put("isActive", language.getIsActive());
        
        // If setting as default, unset other default languages
        if (Boolean.TRUE.equals(request.getIsDefault()) && !language.getIsDefault()) {
            operatorLanguageRepository.findByOperatorIdAndIsDefaultTrue(operatorId)
                .ifPresent(existingDefault -> {
                    if (!existingDefault.getId().equals(language.getId())) {
                        existingDefault.setIsDefault(false);
                        existingDefault.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
                        operatorLanguageRepository.save(existingDefault);
                    }
                });
        }
        
        // Update fields
        if (request.getLanguageName() != null) {
            // For custom languages, validate name is not empty
            if (language.getIsCustom() && request.getLanguageName().trim().isEmpty()) {
                throw new IllegalArgumentException("Language name cannot be empty for custom languages");
            }
            language.setLanguageName(request.getLanguageName());
        }
        if (request.getIsDefault() != null) {
            language.setIsDefault(request.getIsDefault());
        }
        if (request.getIsActive() != null) {
            language.setIsActive(request.getIsActive());
        }
        language.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorLanguage saved = operatorLanguageRepository.save(language);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("languageName", saved.getLanguageName());
        newValues.put("isDefault", saved.getIsDefault());
        newValues.put("isActive", saved.getIsActive());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_LANGUAGE_UPDATED",
            String.format("Updated language %s for operator %d", languageCode, operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Remove a language from an operator.
     */
    @Transactional
    public void removeLanguage(Long operatorId, String languageCode) {
        log.info("Removing language {} from operator {}", languageCode, operatorId);
        
        OperatorLanguage language = operatorLanguageRepository.findByOperatorIdAndLanguageCode(operatorId, languageCode.toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Language %s not found for operator %d", languageCode, operatorId)));
        
        // Don't allow removing the default language if it's the only language
        if (language.getIsDefault()) {
            long activeCount = operatorLanguageRepository.countByOperatorIdAndIsActiveTrue(operatorId);
            if (activeCount == 1) {
                throw new IllegalStateException("Cannot remove the default language when it's the only active language");
            }
        }
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("languageCode", language.getLanguageCode());
        oldValues.put("isDefault", language.getIsDefault());
        oldValues.put("isActive", language.getIsActive());
        
        operatorLanguageRepository.delete(language);
        
        // If removed language was default, set another language as default
        if (language.getIsDefault()) {
            operatorLanguageRepository.findByOperatorIdAndIsActiveTrueOrderByIsDefaultDescLanguageCodeAsc(operatorId)
                .stream()
                .findFirst()
                .ifPresent(newDefault -> {
                    newDefault.setIsDefault(true);
                    newDefault.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
                    operatorLanguageRepository.save(newDefault);
                });
        }
        
        // Log audit event
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_LANGUAGE_REMOVED",
            String.format("Removed language %s from operator %d", languageCode, operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            null
        );
    }
    
    /**
     * Set a language as default for an operator.
     */
    @Transactional
    public OperatorLanguageResponse setDefaultLanguage(Long operatorId, String languageCode) {
        log.info("Setting language {} as default for operator {}", languageCode, operatorId);
        
        OperatorLanguage language = operatorLanguageRepository.findByOperatorIdAndLanguageCode(operatorId, languageCode.toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Language %s not found for operator %d", languageCode, operatorId)));
        
        if (!language.getIsActive()) {
            throw new IllegalStateException("Cannot set inactive language as default");
        }
        
        // Unset other default languages
        operatorLanguageRepository.findByOperatorIdAndIsDefaultTrue(operatorId)
            .ifPresent(existingDefault -> {
                if (!existingDefault.getId().equals(language.getId())) {
                    existingDefault.setIsDefault(false);
                    existingDefault.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
                    operatorLanguageRepository.save(existingDefault);
                }
            });
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("isDefault", language.getIsDefault());
        
        language.setIsDefault(true);
        language.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorLanguage saved = operatorLanguageRepository.save(language);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isDefault", true);
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_LANGUAGE_SET_DEFAULT",
            String.format("Set language %s as default for operator %d", languageCode, operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Get default language for an operator.
     */
    public OperatorLanguageResponse getDefaultLanguage(Long operatorId) {
        log.info("Getting default language for operator {}", operatorId);
        
        OperatorLanguage language = operatorLanguageRepository.findByOperatorIdAndIsDefaultTrue(operatorId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("No default language found for operator %d", operatorId)));
        
        return mapToResponse(language);
    }
    
    /**
     * Check if operator supports a language.
     */
    public boolean supportsLanguage(Long operatorId, String languageCode) {
        return operatorLanguageRepository.existsByOperatorIdAndLanguageCode(operatorId, languageCode.toLowerCase());
    }
    
    private OperatorLanguageResponse mapToResponse(OperatorLanguage language) {
        return OperatorLanguageResponse.builder()
            .id(language.getId())
            .operatorId(language.getOperatorId())
            .languageCode(language.getLanguageCode())
            .isCustom(language.getIsCustom())
            .languageName(language.getLanguageName())
            .isDefault(language.getIsDefault())
            .isActive(language.getIsActive())
            .createdBy(language.getCreatedBy())
            .createdAt(language.getCreatedAt())
            .updatedBy(language.getUpdatedBy())
            .updatedAt(language.getUpdatedAt())
            .build();
    }
}
