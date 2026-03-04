package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateGameBetLimitRequest;
import com.b2bplatform.operator.dto.request.CreateOperatorGameBetLimitRequest;
import com.b2bplatform.operator.dto.request.UpdateGameBetLimitRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorGameBetLimitRequest;
import com.b2bplatform.operator.dto.response.EffectiveBetLimitResponse;
import com.b2bplatform.operator.dto.response.GameBetLimitResponse;
import com.b2bplatform.operator.dto.response.OperatorGameBetLimitResponse;
import com.b2bplatform.operator.model.GameBetLimit;
import com.b2bplatform.operator.model.OperatorGameBetLimit;
import com.b2bplatform.operator.repository.GameBetLimitRepository;
import com.b2bplatform.operator.repository.OperatorGameBetLimitRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing bet limits (game-specific and operator-specific).
 * Implements resolution logic: operator-specific → game-specific → system default.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BetLimitService {
    
    private final GameBetLimitRepository gameBetLimitRepository;
    private final OperatorGameBetLimitRepository operatorGameBetLimitRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    // System default bet limits (fallback when no configuration exists)
    private static final BigDecimal DEFAULT_MIN_BET_USD = new BigDecimal("0.01");
    private static final BigDecimal DEFAULT_MAX_BET_USD = new BigDecimal("10000.00");
    private static final BigDecimal DEFAULT_MIN_BET_EUR = new BigDecimal("0.01");
    private static final BigDecimal DEFAULT_MAX_BET_EUR = new BigDecimal("10000.00");
    
    // ==================== Game-Specific Bet Limits ====================
    
    /**
     * Create game-specific bet limit.
     */
    @Transactional
    public GameBetLimitResponse createGameBetLimit(String gameId, CreateGameBetLimitRequest request) {
        log.info("Creating game-specific bet limit for game: {}, currency: {}", gameId, request.getCurrencyCode());
        
        // Validate min <= max
        if (request.getMinBet().compareTo(request.getMaxBet()) > 0) {
            throw new IllegalArgumentException("Minimum bet must be less than or equal to maximum bet");
        }
        
        // Validate effective dates
        LocalDateTime effectiveFrom = request.getEffectiveFrom() != null 
            ? request.getEffectiveFrom() 
            : LocalDateTime.now();
        
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Effective end date must be after effective start date");
        }
        
        // Check for overlapping active limits
        if (gameBetLimitRepository.existsOverlappingActiveLimit(
            gameId, request.getCurrencyCode(), effectiveFrom, 0L)) {
            throw new IllegalStateException(
                String.format("Active bet limit already exists for game %s and currency %s with effective date %s",
                    gameId, request.getCurrencyCode(), effectiveFrom));
        }
        
        GameBetLimit limit = new GameBetLimit();
        limit.setGameId(gameId);
        limit.setGameProviderId(request.getGameProviderId());
        limit.setCurrencyCode(request.getCurrencyCode());
        limit.setMinBet(request.getMinBet());
        limit.setMaxBet(request.getMaxBet());
        limit.setEffectiveFrom(effectiveFrom);
        limit.setEffectiveTo(request.getEffectiveTo());
        limit.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        GameBetLimit saved = gameBetLimitRepository.save(limit);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("gameId", saved.getGameId());
        newValues.put("currencyCode", saved.getCurrencyCode());
        newValues.put("minBet", saved.getMinBet());
        newValues.put("maxBet", saved.getMaxBet());
        newValues.put("effectiveFrom", saved.getEffectiveFrom());
        
        auditService.logAuditEvent(
            null, // No operator ID for game-specific limits
            "GAME_BET_LIMIT_CREATED",
            String.format("Created game-specific bet limit for game %s, currency %s", gameId, request.getCurrencyCode()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToGameBetLimitResponse(saved);
    }
    
    /**
     * Get game-specific bet limits.
     */
    public List<GameBetLimitResponse> getGameBetLimits(String gameId, String currencyCode, boolean activeOnly) {
        log.info("Getting game-specific bet limits for game: {}, currency: {}, activeOnly: {}", 
            gameId, currencyCode, activeOnly);
        
        List<GameBetLimit> limits;
        if (currencyCode != null) {
            if (activeOnly) {
                Optional<GameBetLimit> limit = gameBetLimitRepository.findActiveByGameAndCurrency(
                    gameId, currencyCode, LocalDateTime.now());
                limits = limit.map(List::of).orElse(List.of());
            } else {
                limits = gameBetLimitRepository.findByGameIdAndCurrencyCodeOrderByEffectiveFromDesc(
                    gameId, currencyCode);
            }
        } else {
            if (activeOnly) {
                limits = gameBetLimitRepository.findActiveByGame(gameId, LocalDateTime.now());
            } else {
                limits = gameBetLimitRepository.findByGameIdOrderByCurrencyCodeAscEffectiveFromDesc(gameId);
            }
        }
        
        return limits.stream()
            .map(this::mapToGameBetLimitResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Update game-specific bet limit.
     */
    @Transactional
    public GameBetLimitResponse updateGameBetLimit(String gameId, Long limitId, UpdateGameBetLimitRequest request) {
        log.info("Updating game-specific bet limit: {}", limitId);
        
        GameBetLimit limit = gameBetLimitRepository.findById(limitId)
            .orElseThrow(() -> new IllegalArgumentException("Bet limit not found: " + limitId));
        
        if (!limit.getGameId().equals(gameId)) {
            throw new IllegalArgumentException("Bet limit does not belong to game: " + gameId);
        }
        
        // Validate min <= max
        if (request.getMinBet().compareTo(request.getMaxBet()) > 0) {
            throw new IllegalArgumentException("Minimum bet must be less than or equal to maximum bet");
        }
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("minBet", limit.getMinBet());
        oldValues.put("maxBet", limit.getMaxBet());
        oldValues.put("effectiveFrom", limit.getEffectiveFrom());
        oldValues.put("effectiveTo", limit.getEffectiveTo());
        
        // Update fields
        limit.setMinBet(request.getMinBet());
        limit.setMaxBet(request.getMaxBet());
        if (request.getEffectiveFrom() != null) {
            limit.setEffectiveFrom(request.getEffectiveFrom());
        }
        if (request.getEffectiveTo() != null) {
            if (request.getEffectiveTo().isBefore(limit.getEffectiveFrom())) {
                throw new IllegalArgumentException("Effective end date must be after effective start date");
            }
            limit.setEffectiveTo(request.getEffectiveTo());
        }
        limit.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        GameBetLimit saved = gameBetLimitRepository.save(limit);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("minBet", saved.getMinBet());
        newValues.put("maxBet", saved.getMaxBet());
        newValues.put("effectiveFrom", saved.getEffectiveFrom());
        newValues.put("effectiveTo", saved.getEffectiveTo());
        
        auditService.logAuditEvent(
            null,
            "GAME_BET_LIMIT_UPDATED",
            String.format("Updated game-specific bet limit %d for game %s", limitId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToGameBetLimitResponse(saved);
    }
    
    /**
     * Deactivate game-specific bet limit.
     */
    @Transactional
    public GameBetLimitResponse deactivateGameBetLimit(String gameId, Long limitId, LocalDateTime effectiveTo) {
        log.info("Deactivating game-specific bet limit: {}", limitId);
        
        GameBetLimit limit = gameBetLimitRepository.findById(limitId)
            .orElseThrow(() -> new IllegalArgumentException("Bet limit not found: " + limitId));
        
        if (!limit.getGameId().equals(gameId)) {
            throw new IllegalArgumentException("Bet limit does not belong to game: " + gameId);
        }
        
        limit.setIsActive(false);
        if (effectiveTo != null) {
            if (effectiveTo.isBefore(limit.getEffectiveFrom())) {
                throw new IllegalArgumentException("Effective end date must be after effective start date");
            }
            limit.setEffectiveTo(effectiveTo);
        } else {
            limit.setEffectiveTo(LocalDateTime.now());
        }
        limit.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        GameBetLimit saved = gameBetLimitRepository.save(limit);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isActive", false);
        newValues.put("effectiveTo", saved.getEffectiveTo());
        
        auditService.logAuditEvent(
            null,
            "GAME_BET_LIMIT_DEACTIVATED",
            String.format("Deactivated game-specific bet limit %d for game %s", limitId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToGameBetLimitResponse(saved);
    }
    
    // ==================== Operator-Specific Bet Limits ====================
    
    /**
     * Create operator-specific bet limit.
     */
    @Transactional
    public OperatorGameBetLimitResponse createOperatorGameBetLimit(
        Long operatorId, String gameId, CreateOperatorGameBetLimitRequest request) {
        log.info("Creating operator-specific bet limit for operator: {}, game: {}, currency: {}", 
            operatorId, gameId, request.getCurrencyCode());
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate min <= max
        if (request.getMinBet().compareTo(request.getMaxBet()) > 0) {
            throw new IllegalArgumentException("Minimum bet must be less than or equal to maximum bet");
        }
        
        // Validate effective dates
        LocalDateTime effectiveFrom = request.getEffectiveFrom() != null 
            ? request.getEffectiveFrom() 
            : LocalDateTime.now();
        
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Effective end date must be after effective start date");
        }
        
        // Check for overlapping active limits
        if (operatorGameBetLimitRepository.existsOverlappingActiveLimit(
            operatorId, gameId, request.getCurrencyCode(), effectiveFrom, 0L)) {
            throw new IllegalStateException(
                String.format("Active bet limit already exists for operator %d, game %s, and currency %s with effective date %s",
                    operatorId, gameId, request.getCurrencyCode(), effectiveFrom));
        }
        
        OperatorGameBetLimit limit = new OperatorGameBetLimit();
        limit.setOperatorId(operatorId);
        limit.setGameId(gameId);
        limit.setGameProviderId(request.getGameProviderId());
        limit.setCurrencyCode(request.getCurrencyCode());
        limit.setMinBet(request.getMinBet());
        limit.setMaxBet(request.getMaxBet());
        limit.setEffectiveFrom(effectiveFrom);
        limit.setEffectiveTo(request.getEffectiveTo());
        limit.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorGameBetLimit saved = operatorGameBetLimitRepository.save(limit);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("operatorId", saved.getOperatorId());
        newValues.put("gameId", saved.getGameId());
        newValues.put("currencyCode", saved.getCurrencyCode());
        newValues.put("minBet", saved.getMinBet());
        newValues.put("maxBet", saved.getMaxBet());
        newValues.put("effectiveFrom", saved.getEffectiveFrom());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_BET_LIMIT_CREATED",
            String.format("Created operator-specific bet limit for operator %d, game %s, currency %s", 
                operatorId, gameId, request.getCurrencyCode()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToOperatorGameBetLimitResponse(saved);
    }
    
    /**
     * Get operator-specific bet limits.
     */
    public List<OperatorGameBetLimitResponse> getOperatorGameBetLimits(
        Long operatorId, String gameId, String currencyCode, boolean activeOnly) {
        log.info("Getting operator-specific bet limits for operator: {}, game: {}, currency: {}, activeOnly: {}", 
            operatorId, gameId, currencyCode, activeOnly);
        
        List<OperatorGameBetLimit> limits;
        if (currencyCode != null) {
            if (activeOnly) {
                Optional<OperatorGameBetLimit> limit = operatorGameBetLimitRepository.findActiveByOperatorAndGameAndCurrency(
                    operatorId, gameId, currencyCode, LocalDateTime.now());
                limits = limit.map(List::of).orElse(List.of());
            } else {
                limits = operatorGameBetLimitRepository.findByOperatorIdAndGameIdAndCurrencyCodeOrderByEffectiveFromDesc(
                    operatorId, gameId, currencyCode);
            }
        } else {
            if (activeOnly) {
                limits = operatorGameBetLimitRepository.findActiveByOperatorAndGame(
                    operatorId, gameId, LocalDateTime.now());
            } else {
                limits = operatorGameBetLimitRepository.findByOperatorIdAndGameIdOrderByCurrencyCodeAscEffectiveFromDesc(
                    operatorId, gameId);
            }
        }
        
        return limits.stream()
            .map(this::mapToOperatorGameBetLimitResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Update operator-specific bet limit.
     */
    @Transactional
    public OperatorGameBetLimitResponse updateOperatorGameBetLimit(
        Long operatorId, String gameId, Long limitId, UpdateOperatorGameBetLimitRequest request) {
        log.info("Updating operator-specific bet limit: {}", limitId);
        
        OperatorGameBetLimit limit = operatorGameBetLimitRepository.findById(limitId)
            .orElseThrow(() -> new IllegalArgumentException("Bet limit not found: " + limitId));
        
        if (!limit.getOperatorId().equals(operatorId) || !limit.getGameId().equals(gameId)) {
            throw new IllegalArgumentException(
                String.format("Bet limit does not belong to operator %d and game %s", operatorId, gameId));
        }
        
        // Validate min <= max
        if (request.getMinBet().compareTo(request.getMaxBet()) > 0) {
            throw new IllegalArgumentException("Minimum bet must be less than or equal to maximum bet");
        }
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("minBet", limit.getMinBet());
        oldValues.put("maxBet", limit.getMaxBet());
        oldValues.put("effectiveFrom", limit.getEffectiveFrom());
        oldValues.put("effectiveTo", limit.getEffectiveTo());
        
        // Update fields
        limit.setMinBet(request.getMinBet());
        limit.setMaxBet(request.getMaxBet());
        if (request.getEffectiveFrom() != null) {
            limit.setEffectiveFrom(request.getEffectiveFrom());
        }
        if (request.getEffectiveTo() != null) {
            if (request.getEffectiveTo().isBefore(limit.getEffectiveFrom())) {
                throw new IllegalArgumentException("Effective end date must be after effective start date");
            }
            limit.setEffectiveTo(request.getEffectiveTo());
        }
        limit.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorGameBetLimit saved = operatorGameBetLimitRepository.save(limit);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("minBet", saved.getMinBet());
        newValues.put("maxBet", saved.getMaxBet());
        newValues.put("effectiveFrom", saved.getEffectiveFrom());
        newValues.put("effectiveTo", saved.getEffectiveTo());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_BET_LIMIT_UPDATED",
            String.format("Updated operator-specific bet limit %d for operator %d, game %s", 
                limitId, operatorId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToOperatorGameBetLimitResponse(saved);
    }
    
    /**
     * Delete operator-specific bet limit.
     */
    @Transactional
    public void deleteOperatorGameBetLimit(Long operatorId, String gameId, Long limitId) {
        log.info("Deleting operator-specific bet limit: {}", limitId);
        
        OperatorGameBetLimit limit = operatorGameBetLimitRepository.findById(limitId)
            .orElseThrow(() -> new IllegalArgumentException("Bet limit not found: " + limitId));
        
        if (!limit.getOperatorId().equals(operatorId) || !limit.getGameId().equals(gameId)) {
            throw new IllegalArgumentException(
                String.format("Bet limit does not belong to operator %d and game %s", operatorId, gameId));
        }
        
        // Store old values for audit
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("operatorId", limit.getOperatorId());
        oldValues.put("gameId", limit.getGameId());
        oldValues.put("currencyCode", limit.getCurrencyCode());
        oldValues.put("minBet", limit.getMinBet());
        oldValues.put("maxBet", limit.getMaxBet());
        
        operatorGameBetLimitRepository.delete(limit);
        
        // Log audit event
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_BET_LIMIT_DELETED",
            String.format("Deleted operator-specific bet limit %d for operator %d, game %s", 
                limitId, operatorId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            null
        );
    }
    
    // ==================== Resolution Logic (For Bet Service) ====================
    
    /**
     * Get effective bet limits with resolution logic.
     * Priority: Operator-specific → Game-specific → System default
     */
    public EffectiveBetLimitResponse getEffectiveBetLimit(Long operatorId, String gameId, String currencyCode) {
        log.debug("Resolving effective bet limit for operator: {}, game: {}, currency: {}", 
            operatorId, gameId, currencyCode);
        
        LocalDateTime now = LocalDateTime.now();
        
        // 1. Check operator-specific limit first (highest priority)
        Optional<OperatorGameBetLimit> operatorLimit = operatorGameBetLimitRepository
            .findActiveByOperatorAndGameAndCurrency(operatorId, gameId, currencyCode, now);
        
        if (operatorLimit.isPresent()) {
            OperatorGameBetLimit limit = operatorLimit.get();
            return EffectiveBetLimitResponse.builder()
                .operatorId(operatorId)
                .gameId(gameId)
                .currencyCode(currencyCode)
                .minBet(limit.getMinBet())
                .maxBet(limit.getMaxBet())
                .source("OPERATOR_SPECIFIC")
                .limitId(limit.getId())
                .isActive(limit.getIsActive())
                .build();
        }
        
        // 2. Check game-specific limit (fallback)
        Optional<GameBetLimit> gameLimit = gameBetLimitRepository
            .findActiveByGameAndCurrency(gameId, currencyCode, now);
        
        if (gameLimit.isPresent()) {
            GameBetLimit limit = gameLimit.get();
            return EffectiveBetLimitResponse.builder()
                .operatorId(operatorId)
                .gameId(gameId)
                .currencyCode(currencyCode)
                .minBet(limit.getMinBet())
                .maxBet(limit.getMaxBet())
                .source("GAME_SPECIFIC")
                .limitId(limit.getId())
                .isActive(limit.getIsActive())
                .build();
        }
        
        // 3. Return system defaults (lowest priority)
        BigDecimal minBet = getSystemDefaultMinBet(currencyCode);
        BigDecimal maxBet = getSystemDefaultMaxBet(currencyCode);
        
        return EffectiveBetLimitResponse.builder()
            .operatorId(operatorId)
            .gameId(gameId)
            .currencyCode(currencyCode)
            .minBet(minBet)
            .maxBet(maxBet)
            .source("SYSTEM_DEFAULT")
            .limitId(null)
            .isActive(true)
            .build();
    }
    
    /**
     * Get system default minimum bet for currency.
     */
    private BigDecimal getSystemDefaultMinBet(String currencyCode) {
        // For now, use same defaults for all currencies
        // Can be extended to have currency-specific defaults
        return DEFAULT_MIN_BET_USD;
    }
    
    /**
     * Get system default maximum bet for currency.
     */
    private BigDecimal getSystemDefaultMaxBet(String currencyCode) {
        // For now, use same defaults for all currencies
        // Can be extended to have currency-specific defaults
        return DEFAULT_MAX_BET_USD;
    }
    
    // ==================== Mapping Methods ====================
    
    private GameBetLimitResponse mapToGameBetLimitResponse(GameBetLimit limit) {
        return GameBetLimitResponse.builder()
            .id(limit.getId())
            .gameId(limit.getGameId())
            .gameProviderId(limit.getGameProviderId())
            .currencyCode(limit.getCurrencyCode())
            .minBet(limit.getMinBet())
            .maxBet(limit.getMaxBet())
            .isActive(limit.getIsActive())
            .effectiveFrom(limit.getEffectiveFrom())
            .effectiveTo(limit.getEffectiveTo())
            .createdBy(limit.getCreatedBy())
            .createdAt(limit.getCreatedAt())
            .updatedBy(limit.getUpdatedBy())
            .updatedAt(limit.getUpdatedAt())
            .build();
    }
    
    private OperatorGameBetLimitResponse mapToOperatorGameBetLimitResponse(OperatorGameBetLimit limit) {
        return OperatorGameBetLimitResponse.builder()
            .id(limit.getId())
            .operatorId(limit.getOperatorId())
            .gameId(limit.getGameId())
            .gameProviderId(limit.getGameProviderId())
            .currencyCode(limit.getCurrencyCode())
            .minBet(limit.getMinBet())
            .maxBet(limit.getMaxBet())
            .isActive(limit.getIsActive())
            .effectiveFrom(limit.getEffectiveFrom())
            .effectiveTo(limit.getEffectiveTo())
            .createdBy(limit.getCreatedBy())
            .createdAt(limit.getCreatedAt())
            .updatedBy(limit.getUpdatedBy())
            .updatedAt(limit.getUpdatedAt())
            .build();
    }
}
