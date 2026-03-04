package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateCommissionConfigRequest;
import com.b2bplatform.operator.dto.request.UpdateCommissionConfigRequest;
import com.b2bplatform.operator.dto.response.CommissionCalculationResponse;
import com.b2bplatform.operator.dto.response.CommissionConfigResponse;
import com.b2bplatform.operator.model.CommissionModelType;
import com.b2bplatform.operator.model.OperatorCommissionCalculation;
import com.b2bplatform.operator.model.OperatorCommissionConfig;
import com.b2bplatform.operator.repository.OperatorCommissionCalculationRepository;
import com.b2bplatform.operator.repository.OperatorCommissionConfigRepository;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing commission configurations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionConfigService {
    
    private final OperatorCommissionConfigRepository commissionConfigRepository;
    private final OperatorCommissionCalculationRepository calculationRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");
    
    /**
     * Create commission configuration.
     */
    @Transactional
    public CommissionConfigResponse createCommissionConfig(Long operatorId, CreateCommissionConfigRequest request) {
        log.info("Creating commission config for operator: {}, provider: {}, model: {}", 
            operatorId, request.getGameProviderId(), request.getCommissionModel());
        
        // Validate operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found with id: " + operatorId);
        }
        
        // Validate commission model-specific fields
        validateCommissionConfig(request);
        
        // Check for overlapping active configs
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effectiveFrom = request.getEffectiveFrom() != null ? request.getEffectiveFrom() : now;
        
        if (commissionConfigRepository.existsActiveConfig(operatorId, request.getGameProviderId(), 
            request.getGameId(), effectiveFrom)) {
            throw new IllegalArgumentException(
                "Active commission configuration already exists for operator: " + operatorId + 
                ", provider: " + request.getGameProviderId() + 
                (request.getGameId() != null ? ", game: " + request.getGameId() : ""));
        }
        
        // Create commission config
        OperatorCommissionConfig config = new OperatorCommissionConfig();
        config.setOperatorId(operatorId);
        config.setGameProviderId(request.getGameProviderId());
        config.setGameId(request.getGameId());
        config.setCommissionModel(request.getCommissionModel());
        
        // Set model-specific fields
        setModelSpecificFields(config, request);
        
        config.setEffectiveFrom(effectiveFrom);
        config.setEffectiveTo(request.getEffectiveTo());
        config.setIsActive(true);
        config.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorCommissionConfig saved = commissionConfigRepository.save(config);
        log.info("Commission config created successfully with id: {}", saved.getId());
        
        // Log audit event
        Map<String, Object> newValues = buildConfigMap(saved);
        auditService.logAuditEvent(
            operatorId,
            "COMMISSION_CONFIG_CREATED",
            "Commission config created: " + request.getCommissionModel() + " for provider: " + request.getGameProviderId(),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return toResponse(saved);
    }
    
    /**
     * Get commission configurations for an operator.
     */
    public List<CommissionConfigResponse> getCommissionConfigs(Long operatorId, String gameProviderId, 
                                                                String gameId, Boolean activeOnly) {
        log.debug("Fetching commission configs for operator: {}, provider: {}, game: {}, activeOnly: {}", 
            operatorId, gameProviderId, gameId, activeOnly);
        
        List<OperatorCommissionConfig> configs;
        
        if (activeOnly != null && activeOnly) {
            LocalDateTime now = LocalDateTime.now();
            if (gameProviderId != null) {
                if (gameId != null) {
                    configs = commissionConfigRepository.findActiveConfig(operatorId, gameProviderId, gameId, now);
                } else {
                    configs = commissionConfigRepository.findByOperatorIdAndGameProviderIdAndIsActiveTrue(
                        operatorId, gameProviderId);
                }
            } else {
                configs = commissionConfigRepository.findByOperatorIdAndIsActiveTrue(operatorId);
            }
        } else {
            configs = commissionConfigRepository.findByOperatorIdOrderByCreatedAtDesc(operatorId);
            if (gameProviderId != null) {
                configs = configs.stream()
                    .filter(c -> c.getGameProviderId().equals(gameProviderId))
                    .collect(Collectors.toList());
            }
            if (gameId != null) {
                configs = configs.stream()
                    .filter(c -> gameId.equals(c.getGameId()))
                    .collect(Collectors.toList());
            }
        }
        
        return configs.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get active commission configuration for Settlement Service (internal endpoint).
     */
    public Optional<CommissionConfigResponse> getActiveCommissionConfig(Long operatorId, String gameProviderId, String gameId) {
        log.debug("Getting active commission config for operator: {}, provider: {}, game: {}", 
            operatorId, gameProviderId, gameId);
        
        LocalDateTime now = LocalDateTime.now();
        List<OperatorCommissionConfig> configs = commissionConfigRepository.findActiveConfig(
            operatorId, gameProviderId, gameId, now);
        
        if (configs.isEmpty()) {
            return Optional.empty();
        }
        
        // Prefer game-specific config over provider-level config
        OperatorCommissionConfig config = configs.get(0);
        return Optional.of(toResponse(config));
    }
    
    /**
     * Update commission configuration.
     */
    @Transactional
    public CommissionConfigResponse updateCommissionConfig(Long operatorId, Long configId, UpdateCommissionConfigRequest request) {
        log.info("Updating commission config: {} for operator: {}", configId, operatorId);
        
        OperatorCommissionConfig config = commissionConfigRepository.findById(configId)
            .orElseThrow(() -> new IllegalArgumentException("Commission config not found with id: " + configId));
        
        if (!config.getOperatorId().equals(operatorId)) {
            throw new IllegalArgumentException("Commission config does not belong to operator: " + operatorId);
        }
        
        // Capture old values for audit
        Map<String, Object> oldValues = buildConfigMap(config);
        
        // Update fields
        if (request.getOperatorGgrRate() != null) {
            config.setOperatorGgrRate(request.getOperatorGgrRate());
        }
        if (request.getProviderGgrRate() != null) {
            config.setProviderGgrRate(request.getProviderGgrRate());
        }
        if (request.getFixedPricePerBet() != null) {
            config.setFixedPricePerBet(request.getFixedPricePerBet());
        }
        if (request.getFixedPriceCurrency() != null) {
            config.setFixedPriceCurrency(request.getFixedPriceCurrency());
        }
        if (request.getWinningsCommissionRate() != null) {
            config.setWinningsCommissionRate(request.getWinningsCommissionRate());
        }
        if (request.getOperatorWinningsShare() != null) {
            config.setOperatorWinningsShare(request.getOperatorWinningsShare());
        }
        if (request.getProviderWinningsShare() != null) {
            config.setProviderWinningsShare(request.getProviderWinningsShare());
        }
        if (request.getEffectiveFrom() != null) {
            config.setEffectiveFrom(request.getEffectiveFrom());
        }
        if (request.getEffectiveTo() != null) {
            config.setEffectiveTo(request.getEffectiveTo());
        }
        if (request.getIsActive() != null) {
            config.setIsActive(request.getIsActive());
        }
        
        config.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        // Validate updated config
        validateCommissionConfig(config);
        
        OperatorCommissionConfig updated = commissionConfigRepository.save(config);
        log.info("Commission config updated successfully with id: {}", updated.getId());
        
        // Log audit event
        Map<String, Object> newValues = buildConfigMap(updated);
        auditService.logAuditEvent(
            operatorId,
            "COMMISSION_CONFIG_UPDATED",
            "Commission config updated: " + updated.getCommissionModel(),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return toResponse(updated);
    }
    
    /**
     * Deactivate commission configuration.
     */
    @Transactional
    public CommissionConfigResponse deactivateCommissionConfig(Long operatorId, Long configId, LocalDateTime effectiveTo, String reason) {
        log.info("Deactivating commission config: {} for operator: {}", configId, operatorId);
        
        OperatorCommissionConfig config = commissionConfigRepository.findById(configId)
            .orElseThrow(() -> new IllegalArgumentException("Commission config not found with id: " + configId));
        
        if (!config.getOperatorId().equals(operatorId)) {
            throw new IllegalArgumentException("Commission config does not belong to operator: " + operatorId);
        }
        
        // Capture old values for audit
        Map<String, Object> oldValues = buildConfigMap(config);
        
        config.setIsActive(false);
        if (effectiveTo != null) {
            config.setEffectiveTo(effectiveTo);
        } else {
            config.setEffectiveTo(LocalDateTime.now());
        }
        config.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorCommissionConfig updated = commissionConfigRepository.save(config);
        log.info("Commission config deactivated successfully with id: {}", updated.getId());
        
        // Log audit event
        Map<String, Object> newValues = buildConfigMap(updated);
        auditService.logAuditEvent(
            operatorId,
            "COMMISSION_CONFIG_DEACTIVATED",
            "Commission config deactivated: " + updated.getCommissionModel() + 
            (reason != null ? " - Reason: " + reason : ""),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return toResponse(updated);
    }
    
    /**
     * Get commission calculations.
     */
    public Page<CommissionCalculationResponse> getCommissionCalculations(Long operatorId, String gameProviderId, 
                                                                          LocalDateTime startDate, LocalDateTime endDate, 
                                                                          Long settlementCycleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "calculatedAt"));
        
        Page<OperatorCommissionCalculation> calculations;
        if (settlementCycleId != null) {
            List<OperatorCommissionCalculation> list = calculationRepository.findBySettlementCycleId(settlementCycleId);
            // Convert to page (simplified - in production, add proper pagination support)
            calculations = Page.empty(pageable);
        } else if (startDate != null && endDate != null) {
            calculations = calculationRepository.findByOperatorIdAndDateRange(operatorId, startDate, endDate, pageable);
        } else if (gameProviderId != null) {
            calculations = calculationRepository.findByOperatorIdAndGameProviderIdOrderByCalculatedAtDesc(
                operatorId, gameProviderId, pageable);
        } else {
            calculations = calculationRepository.findByOperatorIdOrderByCalculatedAtDesc(operatorId, pageable);
        }
        
        return calculations.map(this::toCalculationResponse);
    }
    
    /**
     * Record commission calculation (called by Settlement Service).
     */
    @Transactional
    public CommissionCalculationResponse recordCommissionCalculation(OperatorCommissionCalculation calculation) {
        log.info("Recording commission calculation for operator: {}, provider: {}", 
            calculation.getOperatorId(), calculation.getGameProviderId());
        
        OperatorCommissionCalculation saved = calculationRepository.save(calculation);
        return toCalculationResponse(saved);
    }
    
    /**
     * Validate commission configuration.
     */
    private void validateCommissionConfig(CreateCommissionConfigRequest request) {
        CommissionModelType model = request.getCommissionModel();
        
        switch (model) {
            case GGR_BASED:
                if (request.getOperatorGgrRate() == null || request.getProviderGgrRate() == null) {
                    throw new IllegalArgumentException("Both operator and provider GGR rates are required for GGR_BASED model");
                }
                BigDecimal ggrSum = request.getOperatorGgrRate().add(request.getProviderGgrRate());
                if (ggrSum.subtract(HUNDRED).abs().compareTo(TOLERANCE) > 0) {
                    throw new IllegalArgumentException(
                        "Operator and provider GGR rates must sum to 100.00% (±0.01). " +
                        "Current sum: " + ggrSum + "%");
                }
                break;
                
            case FIXED_PRICE_PER_BET:
                if (request.getFixedPricePerBet() == null || request.getFixedPricePerBet().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Fixed price per bet must be greater than 0");
                }
                if (request.getFixedPriceCurrency() == null || request.getFixedPriceCurrency().length() != 3) {
                    throw new IllegalArgumentException("Fixed price currency is required and must be 3 characters");
                }
                break;
                
            case WINNINGS_BASED:
                if (request.getWinningsCommissionRate() == null || 
                    request.getOperatorWinningsShare() == null || 
                    request.getProviderWinningsShare() == null) {
                    throw new IllegalArgumentException(
                        "Winnings commission rate, operator share, and provider share are required for WINNINGS_BASED model");
                }
                BigDecimal winningsSum = request.getOperatorWinningsShare().add(request.getProviderWinningsShare());
                if (winningsSum.subtract(HUNDRED).abs().compareTo(TOLERANCE) > 0) {
                    throw new IllegalArgumentException(
                        "Operator and provider winnings shares must sum to 100.00% (±0.01). " +
                        "Current sum: " + winningsSum + "%");
                }
                break;
        }
    }
    
    /**
     * Validate commission configuration from entity.
     */
    private void validateCommissionConfig(OperatorCommissionConfig config) {
        CommissionModelType model = config.getCommissionModel();
        
        switch (model) {
            case GGR_BASED:
                if (config.getOperatorGgrRate() == null || config.getProviderGgrRate() == null) {
                    throw new IllegalArgumentException("Both operator and provider GGR rates are required for GGR_BASED model");
                }
                BigDecimal ggrSum = config.getOperatorGgrRate().add(config.getProviderGgrRate());
                if (ggrSum.subtract(HUNDRED).abs().compareTo(TOLERANCE) > 0) {
                    throw new IllegalArgumentException(
                        "Operator and provider GGR rates must sum to 100.00% (±0.01). Current sum: " + ggrSum + "%");
                }
                break;
                
            case FIXED_PRICE_PER_BET:
                if (config.getFixedPricePerBet() == null || config.getFixedPricePerBet().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Fixed price per bet must be greater than 0");
                }
                if (config.getFixedPriceCurrency() == null || config.getFixedPriceCurrency().length() != 3) {
                    throw new IllegalArgumentException("Fixed price currency is required and must be 3 characters");
                }
                break;
                
            case WINNINGS_BASED:
                if (config.getWinningsCommissionRate() == null || 
                    config.getOperatorWinningsShare() == null || 
                    config.getProviderWinningsShare() == null) {
                    throw new IllegalArgumentException(
                        "Winnings commission rate, operator share, and provider share are required for WINNINGS_BASED model");
                }
                BigDecimal winningsSum = config.getOperatorWinningsShare().add(config.getProviderWinningsShare());
                if (winningsSum.subtract(HUNDRED).abs().compareTo(TOLERANCE) > 0) {
                    throw new IllegalArgumentException(
                        "Operator and provider winnings shares must sum to 100.00% (±0.01). Current sum: " + winningsSum + "%");
                }
                break;
        }
    }
    
    /**
     * Set model-specific fields from request.
     */
    private void setModelSpecificFields(OperatorCommissionConfig config, CreateCommissionConfigRequest request) {
        switch (request.getCommissionModel()) {
            case GGR_BASED:
                config.setOperatorGgrRate(request.getOperatorGgrRate());
                config.setProviderGgrRate(request.getProviderGgrRate());
                config.setFixedPricePerBet(null);
                config.setFixedPriceCurrency(null);
                config.setWinningsCommissionRate(null);
                config.setOperatorWinningsShare(null);
                config.setProviderWinningsShare(null);
                break;
                
            case FIXED_PRICE_PER_BET:
                config.setFixedPricePerBet(request.getFixedPricePerBet());
                config.setFixedPriceCurrency(request.getFixedPriceCurrency());
                config.setOperatorGgrRate(null);
                config.setProviderGgrRate(null);
                config.setWinningsCommissionRate(null);
                config.setOperatorWinningsShare(null);
                config.setProviderWinningsShare(null);
                break;
                
            case WINNINGS_BASED:
                config.setWinningsCommissionRate(request.getWinningsCommissionRate());
                config.setOperatorWinningsShare(request.getOperatorWinningsShare());
                config.setProviderWinningsShare(request.getProviderWinningsShare());
                config.setOperatorGgrRate(null);
                config.setProviderGgrRate(null);
                config.setFixedPricePerBet(null);
                config.setFixedPriceCurrency(null);
                break;
        }
    }
    
    /**
     * Build config map for audit logging.
     */
    private Map<String, Object> buildConfigMap(OperatorCommissionConfig config) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", config.getId());
        map.put("operatorId", config.getOperatorId());
        map.put("gameProviderId", config.getGameProviderId());
        map.put("gameId", config.getGameId());
        map.put("commissionModel", config.getCommissionModel());
        map.put("operatorGgrRate", config.getOperatorGgrRate());
        map.put("providerGgrRate", config.getProviderGgrRate());
        map.put("fixedPricePerBet", config.getFixedPricePerBet());
        map.put("fixedPriceCurrency", config.getFixedPriceCurrency());
        map.put("winningsCommissionRate", config.getWinningsCommissionRate());
        map.put("operatorWinningsShare", config.getOperatorWinningsShare());
        map.put("providerWinningsShare", config.getProviderWinningsShare());
        map.put("isActive", config.getIsActive());
        map.put("effectiveFrom", config.getEffectiveFrom());
        map.put("effectiveTo", config.getEffectiveTo());
        return map;
    }
    
    /**
     * Convert OperatorCommissionConfig entity to CommissionConfigResponse DTO.
     */
    private CommissionConfigResponse toResponse(OperatorCommissionConfig config) {
        return CommissionConfigResponse.builder()
            .id(config.getId())
            .operatorId(config.getOperatorId())
            .gameProviderId(config.getGameProviderId())
            .gameId(config.getGameId())
            .commissionModel(config.getCommissionModel())
            .operatorGgrRate(config.getOperatorGgrRate())
            .providerGgrRate(config.getProviderGgrRate())
            .fixedPricePerBet(config.getFixedPricePerBet())
            .fixedPriceCurrency(config.getFixedPriceCurrency())
            .winningsCommissionRate(config.getWinningsCommissionRate())
            .operatorWinningsShare(config.getOperatorWinningsShare())
            .providerWinningsShare(config.getProviderWinningsShare())
            .isActive(config.getIsActive())
            .effectiveFrom(config.getEffectiveFrom())
            .effectiveTo(config.getEffectiveTo())
            .createdBy(config.getCreatedBy())
            .createdAt(config.getCreatedAt())
            .updatedBy(config.getUpdatedBy())
            .updatedAt(config.getUpdatedAt())
            .build();
    }
    
    /**
     * Convert OperatorCommissionCalculation entity to CommissionCalculationResponse DTO.
     */
    private CommissionCalculationResponse toCalculationResponse(OperatorCommissionCalculation calculation) {
        return CommissionCalculationResponse.builder()
            .id(calculation.getId())
            .operatorId(calculation.getOperatorId())
            .gameProviderId(calculation.getGameProviderId())
            .gameId(calculation.getGameId())
            .commissionConfigId(calculation.getCommissionConfigId())
            .commissionModel(calculation.getCommissionModel())
            .calculationPeriodStart(calculation.getCalculationPeriodStart())
            .calculationPeriodEnd(calculation.getCalculationPeriodEnd())
            .totalBets(calculation.getTotalBets())
            .totalWinnings(calculation.getTotalWinnings())
            .ggr(calculation.getGgr())
            .operatorCommission(calculation.getOperatorCommission())
            .providerCommission(calculation.getProviderCommission())
            .numberOfBets(calculation.getNumberOfBets())
            .fixedPricePerBet(calculation.getFixedPricePerBet())
            .totalOperatorFee(calculation.getTotalOperatorFee())
            .totalWinningsAmount(calculation.getTotalWinningsAmount())
            .winningsCommissionRate(calculation.getWinningsCommissionRate())
            .totalCommission(calculation.getTotalCommission())
            .operatorCommissionShare(calculation.getOperatorCommissionShare())
            .providerCommissionShare(calculation.getProviderCommissionShare())
            .currency(calculation.getCurrency())
            .calculatedAt(calculation.getCalculatedAt())
            .calculatedBy(calculation.getCalculatedBy())
            .settlementCycleId(calculation.getSettlementCycleId())
            .build();
    }
}
