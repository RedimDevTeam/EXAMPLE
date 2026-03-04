package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.AddOperatorCurrencyRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorCurrencyRequest;
import com.b2bplatform.operator.dto.response.OperatorCurrencyResponse;
import com.b2bplatform.operator.model.OperatorCurrency;
import com.b2bplatform.operator.repository.OperatorCurrencyRepository;
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
 * Service for managing operator currencies (multi-currency support).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorCurrencyService {
    
    private final OperatorCurrencyRepository operatorCurrencyRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    /**
     * Add a currency to an operator.
     */
    @Transactional
    public OperatorCurrencyResponse addCurrency(Long operatorId, AddOperatorCurrencyRequest request) {
        log.info("Adding currency {} to operator {}", request.getCurrencyCode(), operatorId);
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate currency code format based on type
        String currencyCodeUpper = request.getCurrencyCode().toUpperCase();
        boolean isCustom = Boolean.TRUE.equals(request.getIsCustom());
        
        if (isCustom) {
            // Custom currency: 1-10 alphanumeric characters
            if (currencyCodeUpper.length() < 1 || currencyCodeUpper.length() > 10) {
                throw new IllegalArgumentException("Custom currency code must be between 1 and 10 characters");
            }
            if (!currencyCodeUpper.matches("^[A-Z0-9]+$")) {
                throw new IllegalArgumentException("Custom currency code must contain only uppercase letters and numbers");
            }
            // Custom currencies require a name
            if (request.getCurrencyName() == null || request.getCurrencyName().trim().isEmpty()) {
                throw new IllegalArgumentException("Currency name is required for custom currencies");
            }
        } else {
            // Standard ISO 4217: exactly 3 uppercase letters
            if (currencyCodeUpper.length() != 3) {
                throw new IllegalArgumentException("Standard currency code must be exactly 3 characters (ISO 4217)");
            }
            if (!currencyCodeUpper.matches("^[A-Z]{3}$")) {
                throw new IllegalArgumentException("Standard currency code must contain only uppercase letters");
            }
        }
        
        // Check if currency already exists for this operator
        if (operatorCurrencyRepository.existsByOperatorIdAndCurrencyCode(operatorId, currencyCodeUpper)) {
            throw new IllegalStateException(
                String.format("Currency %s already exists for operator %d", currencyCodeUpper, operatorId));
        }
        
        // If setting as default, unset other default currencies
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            operatorCurrencyRepository.findByOperatorIdAndIsDefaultTrue(operatorId)
                .ifPresent(existingDefault -> {
                    existingDefault.setIsDefault(false);
                    existingDefault.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
                    operatorCurrencyRepository.save(existingDefault);
                });
        }
        
        OperatorCurrency currency = new OperatorCurrency();
        currency.setOperatorId(operatorId);
        currency.setCurrencyCode(currencyCodeUpper);
        currency.setIsCustom(isCustom);
        currency.setCurrencyName(request.getCurrencyName());
        currency.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        currency.setIsActive(true);
        currency.setExchangeRate(request.getExchangeRate());
        currency.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorCurrency saved = operatorCurrencyRepository.save(currency);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("currencyCode", saved.getCurrencyCode());
        newValues.put("isCustom", saved.getIsCustom());
        newValues.put("currencyName", saved.getCurrencyName());
        newValues.put("isDefault", saved.getIsDefault());
        newValues.put("isActive", saved.getIsActive());
        newValues.put("exchangeRate", saved.getExchangeRate());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_CURRENCY_ADDED",
            String.format("Added currency %s to operator %d", request.getCurrencyCode(), operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Get all currencies for an operator.
     */
    public List<OperatorCurrencyResponse> getCurrencies(Long operatorId, Boolean activeOnly) {
        log.info("Getting currencies for operator {}, activeOnly: {}", operatorId, activeOnly);
        
        List<OperatorCurrency> currencies;
        if (Boolean.TRUE.equals(activeOnly)) {
            currencies = operatorCurrencyRepository.findByOperatorIdAndIsActiveTrueOrderByIsDefaultDescCurrencyCodeAsc(operatorId);
        } else {
            currencies = operatorCurrencyRepository.findByOperatorIdOrderByIsDefaultDescCurrencyCodeAsc(operatorId);
        }
        
        return currencies.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get a specific currency for an operator.
     */
    public OperatorCurrencyResponse getCurrency(Long operatorId, String currencyCode) {
        log.info("Getting currency {} for operator {}", currencyCode, operatorId);
        
        OperatorCurrency currency = operatorCurrencyRepository.findByOperatorIdAndCurrencyCode(operatorId, currencyCode.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Currency %s not found for operator %d", currencyCode, operatorId)));
        
        return mapToResponse(currency);
    }
    
    /**
     * Update a currency for an operator.
     */
    @Transactional
    public OperatorCurrencyResponse updateCurrency(Long operatorId, String currencyCode, UpdateOperatorCurrencyRequest request) {
        log.info("Updating currency {} for operator {}", currencyCode, operatorId);
        
        OperatorCurrency currency = operatorCurrencyRepository.findByOperatorIdAndCurrencyCode(operatorId, currencyCode.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Currency %s not found for operator %d", currencyCode, operatorId)));
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("isDefault", currency.getIsDefault());
        oldValues.put("isActive", currency.getIsActive());
        oldValues.put("exchangeRate", currency.getExchangeRate());
        
        // If setting as default, unset other default currencies
        if (Boolean.TRUE.equals(request.getIsDefault()) && !currency.getIsDefault()) {
            operatorCurrencyRepository.findByOperatorIdAndIsDefaultTrue(operatorId)
                .ifPresent(existingDefault -> {
                    if (!existingDefault.getId().equals(currency.getId())) {
                        existingDefault.setIsDefault(false);
                        existingDefault.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
                        operatorCurrencyRepository.save(existingDefault);
                    }
                });
        }
        
        // Update fields
        if (request.getIsDefault() != null) {
            currency.setIsDefault(request.getIsDefault());
        }
        if (request.getIsActive() != null) {
            currency.setIsActive(request.getIsActive());
        }
        if (request.getExchangeRate() != null) {
            currency.setExchangeRate(request.getExchangeRate());
        }
        currency.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorCurrency saved = operatorCurrencyRepository.save(currency);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isDefault", saved.getIsDefault());
        newValues.put("isActive", saved.getIsActive());
        newValues.put("exchangeRate", saved.getExchangeRate());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_CURRENCY_UPDATED",
            String.format("Updated currency %s for operator %d", currencyCode, operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Remove a currency from an operator.
     */
    @Transactional
    public void removeCurrency(Long operatorId, String currencyCode) {
        log.info("Removing currency {} from operator {}", currencyCode, operatorId);
        
        OperatorCurrency currency = operatorCurrencyRepository.findByOperatorIdAndCurrencyCode(operatorId, currencyCode.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Currency %s not found for operator %d", currencyCode, operatorId)));
        
        // Don't allow removing the default currency if it's the only currency
        if (currency.getIsDefault()) {
            long activeCount = operatorCurrencyRepository.countByOperatorIdAndIsActiveTrue(operatorId);
            if (activeCount == 1) {
                throw new IllegalStateException("Cannot remove the default currency when it's the only active currency");
            }
        }
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("currencyCode", currency.getCurrencyCode());
        oldValues.put("isDefault", currency.getIsDefault());
        oldValues.put("isActive", currency.getIsActive());
        
        operatorCurrencyRepository.delete(currency);
        
        // If removed currency was default, set another currency as default
        if (currency.getIsDefault()) {
            operatorCurrencyRepository.findByOperatorIdAndIsActiveTrueOrderByIsDefaultDescCurrencyCodeAsc(operatorId)
                .stream()
                .findFirst()
                .ifPresent(newDefault -> {
                    newDefault.setIsDefault(true);
                    newDefault.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
                    operatorCurrencyRepository.save(newDefault);
                });
        }
        
        // Log audit event
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_CURRENCY_REMOVED",
            String.format("Removed currency %s from operator %d", currencyCode, operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            null
        );
    }
    
    /**
     * Set a currency as default for an operator.
     */
    @Transactional
    public OperatorCurrencyResponse setDefaultCurrency(Long operatorId, String currencyCode) {
        log.info("Setting currency {} as default for operator {}", currencyCode, operatorId);
        
        OperatorCurrency currency = operatorCurrencyRepository.findByOperatorIdAndCurrencyCode(operatorId, currencyCode.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Currency %s not found for operator %d", currencyCode, operatorId)));
        
        if (!currency.getIsActive()) {
            throw new IllegalStateException("Cannot set inactive currency as default");
        }
        
        // Unset other default currencies
        operatorCurrencyRepository.findByOperatorIdAndIsDefaultTrue(operatorId)
            .ifPresent(existingDefault -> {
                if (!existingDefault.getId().equals(currency.getId())) {
                    existingDefault.setIsDefault(false);
                    existingDefault.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
                    operatorCurrencyRepository.save(existingDefault);
                }
            });
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("isDefault", currency.getIsDefault());
        
        currency.setIsDefault(true);
        currency.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorCurrency saved = operatorCurrencyRepository.save(currency);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isDefault", true);
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_CURRENCY_SET_DEFAULT",
            String.format("Set currency %s as default for operator %d", currencyCode, operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Get default currency for an operator.
     */
    public OperatorCurrencyResponse getDefaultCurrency(Long operatorId) {
        log.info("Getting default currency for operator {}", operatorId);
        
        OperatorCurrency currency = operatorCurrencyRepository.findByOperatorIdAndIsDefaultTrue(operatorId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("No default currency found for operator %d", operatorId)));
        
        return mapToResponse(currency);
    }
    
    /**
     * Check if operator supports a currency.
     */
    public boolean supportsCurrency(Long operatorId, String currencyCode) {
        return operatorCurrencyRepository.existsByOperatorIdAndCurrencyCode(operatorId, currencyCode.toUpperCase());
    }
    
    private OperatorCurrencyResponse mapToResponse(OperatorCurrency currency) {
        return OperatorCurrencyResponse.builder()
            .id(currency.getId())
            .operatorId(currency.getOperatorId())
            .currencyCode(currency.getCurrencyCode())
            .isCustom(currency.getIsCustom())
            .currencyName(currency.getCurrencyName())
            .isDefault(currency.getIsDefault())
            .isActive(currency.getIsActive())
            .exchangeRate(currency.getExchangeRate())
            .createdBy(currency.getCreatedBy())
            .createdAt(currency.getCreatedAt())
            .updatedBy(currency.getUpdatedBy())
            .updatedAt(currency.getUpdatedAt())
            .build();
    }
}
