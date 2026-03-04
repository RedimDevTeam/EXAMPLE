package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateOperatorGameConfigRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorGameConfigRequest;
import com.b2bplatform.operator.dto.response.GameAvailabilityResponse;
import com.b2bplatform.operator.dto.response.OperatorGameConfigResponse;
import com.b2bplatform.operator.model.OperatorGameConfig;
import com.b2bplatform.operator.repository.OperatorGameConfigRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing operator game configurations.
 * Controls which games are enabled/disabled for each operator.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorGameConfigService {
    
    private final OperatorGameConfigRepository operatorGameConfigRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    /**
     * Create game configuration for an operator.
     */
    @Transactional
    public OperatorGameConfigResponse createGameConfig(Long operatorId, CreateOperatorGameConfigRequest request) {
        log.info("Creating game configuration for operator {}, provider {}, game {}", 
            operatorId, request.getGameProviderId(), request.getGameId());
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate effective dates
        LocalDateTime effectiveFrom = request.getEffectiveFrom() != null 
            ? request.getEffectiveFrom() 
            : LocalDateTime.now();
        
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Effective end date must be after effective start date");
        }
        
        // Check for overlapping active configs
        if (operatorGameConfigRepository.existsOverlappingActiveConfig(
            operatorId, request.getGameProviderId(), request.getGameId(), effectiveFrom, 0L)) {
            throw new IllegalStateException(
                String.format("Active game configuration already exists for operator %d, provider %s, game %s with effective date %s",
                    operatorId, request.getGameProviderId(), request.getGameId(), effectiveFrom));
        }
        
        OperatorGameConfig config = new OperatorGameConfig();
        config.setOperatorId(operatorId);
        config.setGameProviderId(request.getGameProviderId());
        config.setGameId(request.getGameId());
        config.setIsEnabled(Boolean.TRUE.equals(request.getIsEnabled()));
        config.setEffectiveFrom(effectiveFrom);
        config.setEffectiveTo(request.getEffectiveTo());
        config.setLaunchUrl(request.getLaunchUrl());
        config.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorGameConfig saved = operatorGameConfigRepository.save(config);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("gameProviderId", saved.getGameProviderId());
        newValues.put("gameId", saved.getGameId());
        newValues.put("isEnabled", saved.getIsEnabled());
        newValues.put("effectiveFrom", saved.getEffectiveFrom());
        newValues.put("launchUrl", saved.getLaunchUrl());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_GAME_CONFIG_CREATED",
            String.format("Created game configuration for operator %d, provider %s, game %s", 
                operatorId, request.getGameProviderId(), request.getGameId()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Get game configurations for an operator.
     */
    public List<OperatorGameConfigResponse> getGameConfigs(
        Long operatorId, String gameProviderId, Boolean enabledOnly, Boolean activeOnly) {
        log.info("Getting game configurations for operator {}, provider {}, enabledOnly: {}, activeOnly: {}", 
            operatorId, gameProviderId, enabledOnly, activeOnly);
        
        List<OperatorGameConfig> configs;
        LocalDateTime now = LocalDateTime.now();
        
        if (gameProviderId != null) {
            if (Boolean.TRUE.equals(activeOnly)) {
                configs = operatorGameConfigRepository.findActiveByOperatorAndProvider(operatorId, gameProviderId, now);
            } else {
                configs = operatorGameConfigRepository.findByOperatorIdAndGameProviderIdOrderByGameIdAscEffectiveFromDesc(
                    operatorId, gameProviderId);
            }
        } else {
            if (Boolean.TRUE.equals(activeOnly)) {
                configs = operatorGameConfigRepository.findActiveByOperator(operatorId, now);
            } else {
                configs = operatorGameConfigRepository.findByOperatorIdOrderByGameProviderIdAscGameIdAscEffectiveFromDesc(operatorId);
            }
        }
        
        // Filter by enabled if requested
        if (Boolean.TRUE.equals(enabledOnly)) {
            configs = configs.stream()
                .filter(c -> c.getIsEnabled() && c.getIsActive() && 
                    (c.getEffectiveTo() == null || c.getEffectiveTo().isAfter(now)))
                .collect(Collectors.toList());
        }
        
        return configs.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get enabled games for an operator (for game listing).
     */
    public List<OperatorGameConfigResponse> getEnabledGames(Long operatorId) {
        log.info("Getting enabled games for operator {}", operatorId);
        
        List<OperatorGameConfig> configs = operatorGameConfigRepository.findEnabledGamesForOperator(
            operatorId, LocalDateTime.now());
        
        return configs.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get a specific game configuration.
     */
    public OperatorGameConfigResponse getGameConfig(
        Long operatorId, String gameProviderId, String gameId) {
        log.info("Getting game configuration for operator {}, provider {}, game {}", 
            operatorId, gameProviderId, gameId);
        
        OperatorGameConfig config = operatorGameConfigRepository.findActiveByOperatorAndGame(
            operatorId, gameProviderId, gameId, LocalDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Game configuration not found for operator %d, provider %s, game %s", 
                    operatorId, gameProviderId, gameId)));
        
        return mapToResponse(config);
    }
    
    /**
     * Update game configuration.
     */
    @Transactional
    public OperatorGameConfigResponse updateGameConfig(
        Long operatorId, String gameProviderId, String gameId, UpdateOperatorGameConfigRequest request) {
        log.info("Updating game configuration for operator {}, provider {}, game {}", 
            operatorId, gameProviderId, gameId);
        
        OperatorGameConfig config = operatorGameConfigRepository.findActiveByOperatorAndGame(
            operatorId, gameProviderId, gameId, LocalDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Game configuration not found for operator %d, provider %s, game %s", 
                    operatorId, gameProviderId, gameId)));
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("isEnabled", config.getIsEnabled());
        oldValues.put("effectiveFrom", config.getEffectiveFrom());
        oldValues.put("effectiveTo", config.getEffectiveTo());
        oldValues.put("launchUrl", config.getLaunchUrl());
        
        // Update fields
        if (request.getIsEnabled() != null) {
            config.setIsEnabled(request.getIsEnabled());
        }
        if (request.getEffectiveFrom() != null) {
            config.setEffectiveFrom(request.getEffectiveFrom());
        }
        if (request.getEffectiveTo() != null) {
            if (request.getEffectiveTo().isBefore(config.getEffectiveFrom())) {
                throw new IllegalArgumentException("Effective end date must be after effective start date");
            }
            config.setEffectiveTo(request.getEffectiveTo());
        }
        if (request.getLaunchUrl() != null) {
            config.setLaunchUrl(request.getLaunchUrl());
        }
        config.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorGameConfig saved = operatorGameConfigRepository.save(config);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isEnabled", saved.getIsEnabled());
        newValues.put("effectiveFrom", saved.getEffectiveFrom());
        newValues.put("effectiveTo", saved.getEffectiveTo());
        newValues.put("launchUrl", saved.getLaunchUrl());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_GAME_CONFIG_UPDATED",
            String.format("Updated game configuration for operator %d, provider %s, game %s", 
                operatorId, gameProviderId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Enable/disable a game for an operator.
     */
    @Transactional
    public OperatorGameConfigResponse setGameEnabled(
        Long operatorId, String gameProviderId, String gameId, boolean enabled) {
        log.info("Setting game {} enabled={} for operator {}, provider {}", 
            gameId, enabled, operatorId, gameProviderId);
        
        OperatorGameConfig config = operatorGameConfigRepository.findActiveByOperatorAndGame(
            operatorId, gameProviderId, gameId, LocalDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Game configuration not found for operator %d, provider %s, game %s", 
                    operatorId, gameProviderId, gameId)));
        
        // Store old value for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("isEnabled", config.getIsEnabled());
        
        config.setIsEnabled(enabled);
        config.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorGameConfig saved = operatorGameConfigRepository.save(config);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isEnabled", enabled);
        
        auditService.logAuditEvent(
            operatorId,
            enabled ? "OPERATOR_GAME_ENABLED" : "OPERATOR_GAME_DISABLED",
            String.format("Game %s %s for operator %d, provider %s", 
                gameId, enabled ? "enabled" : "disabled", operatorId, gameProviderId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Deactivate game configuration.
     */
    @Transactional
    public OperatorGameConfigResponse deactivateGameConfig(
        Long operatorId, String gameProviderId, String gameId, LocalDateTime effectiveTo) {
        log.info("Deactivating game configuration for operator {}, provider {}, game {}", 
            operatorId, gameProviderId, gameId);
        
        OperatorGameConfig config = operatorGameConfigRepository.findActiveByOperatorAndGame(
            operatorId, gameProviderId, gameId, LocalDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Game configuration not found for operator %d, provider %s, game %s", 
                    operatorId, gameProviderId, gameId)));
        
        config.setIsActive(false);
        if (effectiveTo != null) {
            if (effectiveTo.isBefore(config.getEffectiveFrom())) {
                throw new IllegalArgumentException("Effective end date must be after effective start date");
            }
            config.setEffectiveTo(effectiveTo);
        } else {
            config.setEffectiveTo(LocalDateTime.now());
        }
        config.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorGameConfig saved = operatorGameConfigRepository.save(config);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isActive", false);
        newValues.put("effectiveTo", saved.getEffectiveTo());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_GAME_CONFIG_DEACTIVATED",
            String.format("Deactivated game configuration for operator %d, provider %s, game %s", 
                operatorId, gameProviderId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    // ==================== Internal Endpoints (For Bet Service) ====================
    
    /**
     * Check if a game is available for an operator.
     * Used by Bet Service to validate game availability before processing bets.
     */
    public GameAvailabilityResponse checkGameAvailability(
        Long operatorId, String gameProviderId, String gameId) {
        log.debug("Checking game availability for operator {}, provider {}, game {}", 
            operatorId, gameProviderId, gameId);
        
        LocalDateTime now = LocalDateTime.now();
        
        Optional<OperatorGameConfig> configOpt = operatorGameConfigRepository.findActiveByOperatorAndGame(
            operatorId, gameProviderId, gameId, now);
        
        if (configOpt.isEmpty()) {
            // No configuration found = game not available
            return GameAvailabilityResponse.builder()
                .operatorId(operatorId)
                .gameProviderId(gameProviderId)
                .gameId(gameId)
                .isEnabled(false)
                .isAvailable(false)
                .build();
        }
        
        OperatorGameConfig config = configOpt.get();
        boolean isAvailable = config.getIsEnabled() && config.getIsActive() && 
            (config.getEffectiveTo() == null || config.getEffectiveTo().isAfter(now));
        
        return GameAvailabilityResponse.builder()
            .operatorId(operatorId)
            .gameProviderId(gameProviderId)
            .gameId(gameId)
            .isEnabled(config.getIsEnabled())
            .isAvailable(isAvailable)
            .launchUrl(config.getLaunchUrl())
            .build();
    }
    
    private OperatorGameConfigResponse mapToResponse(OperatorGameConfig config) {
        return OperatorGameConfigResponse.builder()
            .id(config.getId())
            .operatorId(config.getOperatorId())
            .gameProviderId(config.getGameProviderId())
            .gameId(config.getGameId())
            .isEnabled(config.getIsEnabled())
            .isActive(config.getIsActive())
            .effectiveFrom(config.getEffectiveFrom())
            .effectiveTo(config.getEffectiveTo())
            .launchUrl(config.getLaunchUrl())
            .createdBy(config.getCreatedBy())
            .createdAt(config.getCreatedAt())
            .updatedBy(config.getUpdatedBy())
            .updatedAt(config.getUpdatedAt())
            .build();
    }
}
