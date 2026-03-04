package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.*;
import com.b2bplatform.operator.dto.response.BetLimitTypeResponse;
import com.b2bplatform.operator.dto.response.ChipDenominationResponse;
import com.b2bplatform.operator.dto.response.ChipDenominationsResponse;
import com.b2bplatform.operator.model.BetLimitType;
import com.b2bplatform.operator.model.OperatorBetLimitType;
import com.b2bplatform.operator.model.OperatorChipDenomination;
import com.b2bplatform.operator.repository.OperatorBetLimitTypeRepository;
import com.b2bplatform.operator.repository.OperatorChipDenominationRepository;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing chip denominations and bet limit types.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChipConfigService {
    
    private final OperatorChipDenominationRepository chipDenominationRepository;
    private final OperatorBetLimitTypeRepository betLimitTypeRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    // ==================== Chip Denominations ====================
    
    /**
     * Create a single chip denomination.
     */
    @Transactional
    public ChipDenominationResponse createChipDenomination(
        Long operatorId, String gameId, CreateChipDenominationRequest request) {
        log.info("Creating chip denomination for operator {}, game {}, chip index {}", 
            operatorId, gameId, request.getChipIndex());
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate chip index (must be non-negative)
        if (request.getChipIndex() < 0) {
            throw new IllegalArgumentException("Chip index must be non-negative (0 or greater)");
        }
        
        // Check if chip denomination already exists
        if (chipDenominationRepository.existsByOperatorIdAndGameIdAndCurrencyCodeAndChipIndex(
            operatorId, gameId, request.getCurrencyCode(), request.getChipIndex())) {
            throw new IllegalStateException(
                String.format("Chip denomination already exists for operator %d, game %s, currency %s, index %d",
                    operatorId, gameId, request.getCurrencyCode(), request.getChipIndex()));
        }
        
        OperatorChipDenomination chip = new OperatorChipDenomination();
        chip.setOperatorId(operatorId);
        chip.setGameId(gameId);
        chip.setGameProviderId(request.getGameProviderId());
        chip.setCurrencyCode(request.getCurrencyCode());
        chip.setChipIndex(request.getChipIndex());
        chip.setChipValue(request.getChipValue());
        chip.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : request.getChipIndex());
        chip.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorChipDenomination saved = chipDenominationRepository.save(chip);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("chipIndex", saved.getChipIndex());
        newValues.put("chipValue", saved.getChipValue());
        newValues.put("displayOrder", saved.getDisplayOrder());
        
        auditService.logAuditEvent(
            operatorId,
            "CHIP_DENOMINATION_CREATED",
            String.format("Created chip denomination for operator %d, game %s, index %d, value %s", 
                operatorId, gameId, request.getChipIndex(), request.getChipValue()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToChipResponse(saved);
    }
    
    /**
     * Bulk create chip denominations (for Excel upload).
     */
    @Transactional
    public List<ChipDenominationResponse> bulkCreateChipDenominations(
        Long operatorId, String gameId, BulkCreateChipDenominationsRequest request) {
        log.info("Bulk creating chip denominations for operator {}, game {}", operatorId, gameId);
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate chip denominations (reasonable limit for UI space)
        if (request.getChipDenominations().size() > 20) {
            throw new IllegalArgumentException("Cannot create more than 20 chip denominations (practical limit for UI space)");
        }
        
        // Check for duplicate chip indices
        long distinctIndices = request.getChipDenominations().stream()
            .map(CreateChipDenominationRequest::getChipIndex)
            .distinct()
            .count();
        if (distinctIndices != request.getChipDenominations().size()) {
            throw new IllegalArgumentException("Duplicate chip indices found");
        }
        
        // Create all chip denominations
        List<OperatorChipDenomination> chips = request.getChipDenominations().stream()
            .map(req -> {
                // Check if already exists
                if (chipDenominationRepository.existsByOperatorIdAndGameIdAndCurrencyCodeAndChipIndex(
                    operatorId, gameId, request.getCurrencyCode(), req.getChipIndex())) {
                    throw new IllegalStateException(
                        String.format("Chip denomination already exists for index %d", req.getChipIndex()));
                }
                
                OperatorChipDenomination chip = new OperatorChipDenomination();
                chip.setOperatorId(operatorId);
                chip.setGameId(gameId);
                chip.setGameProviderId(request.getGameProviderId());
                chip.setCurrencyCode(request.getCurrencyCode());
                chip.setChipIndex(req.getChipIndex());
                chip.setChipValue(req.getChipValue());
                chip.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : req.getChipIndex());
                chip.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
                return chip;
            })
            .collect(Collectors.toList());
        
        List<OperatorChipDenomination> saved = chipDenominationRepository.saveAll(chips);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("count", saved.size());
        newValues.put("gameId", gameId);
        newValues.put("currencyCode", request.getCurrencyCode());
        
        auditService.logAuditEvent(
            operatorId,
            "CHIP_DENOMINATIONS_BULK_CREATED",
            String.format("Bulk created %d chip denominations for operator %d, game %s", 
                saved.size(), operatorId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return saved.stream()
            .map(this::mapToChipResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get chip denominations for an operator, game, and currency.
     */
    public ChipDenominationsResponse getChipDenominations(
        Long operatorId, String gameId, String currencyCode, Boolean activeOnly) {
        log.info("Getting chip denominations for operator {}, game {}, currency {}, activeOnly: {}", 
            operatorId, gameId, currencyCode, activeOnly);
        
        List<OperatorChipDenomination> chips;
        if (Boolean.TRUE.equals(activeOnly)) {
            chips = chipDenominationRepository.findByOperatorIdAndGameIdAndCurrencyCodeAndIsActiveTrueOrderByDisplayOrderAsc(
                operatorId, gameId, currencyCode);
        } else {
            chips = chipDenominationRepository.findByOperatorIdAndGameIdAndCurrencyCodeOrderByChipIndexAsc(
                operatorId, gameId, currencyCode);
        }
        
        List<ChipDenominationResponse> chipResponses = chips.stream()
            .map(this::mapToChipResponse)
            .collect(Collectors.toList());
        
        return ChipDenominationsResponse.builder()
            .operatorId(operatorId)
            .gameId(gameId)
            .currencyCode(currencyCode)
            .chips(chipResponses)
            .build();
    }
    
    /**
     * Update chip denomination.
     */
    @Transactional
    public ChipDenominationResponse updateChipDenomination(
        Long operatorId, String gameId, String currencyCode, Integer chipIndex, 
        UpdateChipDenominationRequest request) {
        log.info("Updating chip denomination for operator {}, game {}, currency {}, index {}", 
            operatorId, gameId, currencyCode, chipIndex);
        
        OperatorChipDenomination chip = chipDenominationRepository
            .findByOperatorIdAndGameIdAndCurrencyCodeAndChipIndex(operatorId, gameId, currencyCode, chipIndex)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Chip denomination not found for operator %d, game %s, currency %s, index %d",
                    operatorId, gameId, currencyCode, chipIndex)));
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("chipValue", chip.getChipValue());
        oldValues.put("isActive", chip.getIsActive());
        oldValues.put("displayOrder", chip.getDisplayOrder());
        
        // Update fields
        if (request.getChipValue() != null) {
            chip.setChipValue(request.getChipValue());
        }
        if (request.getIsActive() != null) {
            chip.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            chip.setDisplayOrder(request.getDisplayOrder());
        }
        chip.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorChipDenomination saved = chipDenominationRepository.save(chip);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("chipValue", saved.getChipValue());
        newValues.put("isActive", saved.getIsActive());
        newValues.put("displayOrder", saved.getDisplayOrder());
        
        auditService.logAuditEvent(
            operatorId,
            "CHIP_DENOMINATION_UPDATED",
            String.format("Updated chip denomination for operator %d, game %s, index %d", 
                operatorId, gameId, chipIndex),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToChipResponse(saved);
    }
    
    // ==================== Bet Limit Types ====================
    
    /**
     * Create bet limit type.
     */
    @Transactional
    public BetLimitTypeResponse createBetLimitType(
        Long operatorId, String gameId, CreateBetLimitTypeRequest request) {
        log.info("Creating bet limit type {} for operator {}, game {}", 
            request.getLimitType(), operatorId, gameId);
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate min <= max
        if (request.getMinBetLimit().compareTo(request.getMaxBetLimit()) > 0) {
            throw new IllegalArgumentException("Minimum bet limit must be less than or equal to maximum bet limit");
        }
        
        // Check if bet limit type already exists
        if (betLimitTypeRepository.existsByOperatorIdAndGameIdAndCurrencyCodeAndLimitType(
            operatorId, gameId, request.getCurrencyCode(), request.getLimitType())) {
            throw new IllegalStateException(
                String.format("Bet limit type %s already exists for operator %d, game %s, currency %s",
                    request.getLimitType(), operatorId, gameId, request.getCurrencyCode()));
        }
        
        OperatorBetLimitType limitType = new OperatorBetLimitType();
        limitType.setOperatorId(operatorId);
        limitType.setGameId(gameId);
        limitType.setGameProviderId(request.getGameProviderId());
        limitType.setCurrencyCode(request.getCurrencyCode());
        limitType.setLimitType(request.getLimitType());
        limitType.setMinBetLimit(request.getMinBetLimit());
        limitType.setMaxBetLimit(request.getMaxBetLimit());
        limitType.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorBetLimitType saved = betLimitTypeRepository.save(limitType);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("limitType", saved.getLimitType());
        newValues.put("minBetLimit", saved.getMinBetLimit());
        newValues.put("maxBetLimit", saved.getMaxBetLimit());
        
        auditService.logAuditEvent(
            operatorId,
            "BET_LIMIT_TYPE_CREATED",
            String.format("Created bet limit type %s for operator %d, game %s", 
                request.getLimitType(), operatorId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToBetLimitTypeResponse(saved);
    }
    
    /**
     * Get bet limit types for an operator, game, and currency.
     */
    public List<BetLimitTypeResponse> getBetLimitTypes(
        Long operatorId, String gameId, String currencyCode, Boolean activeOnly) {
        log.info("Getting bet limit types for operator {}, game {}, currency {}, activeOnly: {}", 
            operatorId, gameId, currencyCode, activeOnly);
        
        List<OperatorBetLimitType> limitTypes;
        if (Boolean.TRUE.equals(activeOnly)) {
            limitTypes = betLimitTypeRepository.findByOperatorIdAndGameIdAndCurrencyCodeAndIsActiveTrueOrderByLimitTypeAsc(
                operatorId, gameId, currencyCode);
        } else {
            limitTypes = betLimitTypeRepository.findByOperatorIdAndGameIdAndCurrencyCodeOrderByLimitTypeAsc(
                operatorId, gameId, currencyCode);
        }
        
        return limitTypes.stream()
            .map(this::mapToBetLimitTypeResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Update bet limit type.
     */
    @Transactional
    public BetLimitTypeResponse updateBetLimitType(
        Long operatorId, String gameId, String currencyCode, BetLimitType limitTypeEnum,
        UpdateBetLimitTypeRequest request) {
        log.info("Updating bet limit type {} for operator {}, game {}", 
            limitTypeEnum, operatorId, gameId);
        
        OperatorBetLimitType limitType = betLimitTypeRepository
            .findByOperatorIdAndGameIdAndCurrencyCodeAndLimitType(operatorId, gameId, currencyCode, limitTypeEnum)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Bet limit type %s not found for operator %d, game %s, currency %s",
                    limitTypeEnum, operatorId, gameId, currencyCode)));
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("minBetLimit", limitType.getMinBetLimit());
        oldValues.put("maxBetLimit", limitType.getMaxBetLimit());
        oldValues.put("isActive", limitType.getIsActive());
        
        // Validate min <= max if both are provided
        BigDecimal newMin = request.getMinBetLimit() != null ? request.getMinBetLimit() : limitType.getMinBetLimit();
        BigDecimal newMax = request.getMaxBetLimit() != null ? request.getMaxBetLimit() : limitType.getMaxBetLimit();
        if (newMin.compareTo(newMax) > 0) {
            throw new IllegalArgumentException("Minimum bet limit must be less than or equal to maximum bet limit");
        }
        
        // Update fields
        if (request.getMinBetLimit() != null) {
            limitType.setMinBetLimit(request.getMinBetLimit());
        }
        if (request.getMaxBetLimit() != null) {
            limitType.setMaxBetLimit(request.getMaxBetLimit());
        }
        if (request.getIsActive() != null) {
            limitType.setIsActive(request.getIsActive());
        }
        limitType.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorBetLimitType saved = betLimitTypeRepository.save(limitType);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("minBetLimit", saved.getMinBetLimit());
        newValues.put("maxBetLimit", saved.getMaxBetLimit());
        newValues.put("isActive", saved.getIsActive());
        
        auditService.logAuditEvent(
            operatorId,
            "BET_LIMIT_TYPE_UPDATED",
            String.format("Updated bet limit type %s for operator %d, game %s", 
                limitTypeEnum, operatorId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToBetLimitTypeResponse(saved);
    }
    
    // ==================== Mapping Methods ====================
    
    private ChipDenominationResponse mapToChipResponse(OperatorChipDenomination chip) {
        return ChipDenominationResponse.builder()
            .id(chip.getId())
            .operatorId(chip.getOperatorId())
            .gameId(chip.getGameId())
            .gameProviderId(chip.getGameProviderId())
            .currencyCode(chip.getCurrencyCode())
            .chipIndex(chip.getChipIndex())
            .chipValue(chip.getChipValue())
            .isActive(chip.getIsActive())
            .displayOrder(chip.getDisplayOrder())
            .createdBy(chip.getCreatedBy())
            .createdAt(chip.getCreatedAt())
            .updatedBy(chip.getUpdatedBy())
            .updatedAt(chip.getUpdatedAt())
            .build();
    }
    
    private BetLimitTypeResponse mapToBetLimitTypeResponse(OperatorBetLimitType limitType) {
        return BetLimitTypeResponse.builder()
            .id(limitType.getId())
            .operatorId(limitType.getOperatorId())
            .gameId(limitType.getGameId())
            .gameProviderId(limitType.getGameProviderId())
            .currencyCode(limitType.getCurrencyCode())
            .limitType(limitType.getLimitType())
            .minBetLimit(limitType.getMinBetLimit())
            .maxBetLimit(limitType.getMaxBetLimit())
            .isActive(limitType.getIsActive())
            .createdBy(limitType.getCreatedBy())
            .createdAt(limitType.getCreatedAt())
            .updatedBy(limitType.getUpdatedBy())
            .updatedAt(limitType.getUpdatedAt())
            .build();
    }
}
