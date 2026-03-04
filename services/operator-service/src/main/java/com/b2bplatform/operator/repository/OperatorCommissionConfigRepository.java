package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.CommissionModelType;
import com.b2bplatform.operator.model.OperatorCommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OperatorCommissionConfig entity.
 */
@Repository
public interface OperatorCommissionConfigRepository extends JpaRepository<OperatorCommissionConfig, Long> {
    
    /**
     * Find active commission configs for an operator.
     */
    List<OperatorCommissionConfig> findByOperatorIdAndIsActiveTrue(Long operatorId);
    
    /**
     * Find active commission configs for operator and game provider.
     */
    List<OperatorCommissionConfig> findByOperatorIdAndGameProviderIdAndIsActiveTrue(
        Long operatorId, String gameProviderId);
    
    /**
     * Find active commission config for operator, game provider, and game.
     * Returns game-specific config if exists, otherwise provider-level config.
     */
    @Query("SELECT c FROM OperatorCommissionConfig c WHERE " +
           "c.operatorId = :operatorId AND " +
           "c.gameProviderId = :gameProviderId AND " +
           "c.isActive = true AND " +
           "(:gameId IS NULL OR c.gameId IS NULL OR c.gameId = :gameId) AND " +
           "(c.effectiveFrom <= :now AND (c.effectiveTo IS NULL OR c.effectiveTo > :now)) " +
           "ORDER BY CASE WHEN c.gameId IS NULL THEN 1 ELSE 0 END")
    List<OperatorCommissionConfig> findActiveConfig(
        @Param("operatorId") Long operatorId,
        @Param("gameProviderId") String gameProviderId,
        @Param("gameId") String gameId,
        @Param("now") LocalDateTime now);
    
    /**
     * Find commission configs by operator ID.
     */
    List<OperatorCommissionConfig> findByOperatorIdOrderByCreatedAtDesc(Long operatorId);
    
    /**
     * Check if active config exists for operator-provider-game combination.
     */
    @Query("SELECT COUNT(c) > 0 FROM OperatorCommissionConfig c WHERE " +
           "c.operatorId = :operatorId AND " +
           "c.gameProviderId = :gameProviderId AND " +
           "(:gameId IS NULL AND c.gameId IS NULL OR c.gameId = :gameId) AND " +
           "c.isActive = true AND " +
           "(c.effectiveFrom <= :now AND (c.effectiveTo IS NULL OR c.effectiveTo > :now))")
    boolean existsActiveConfig(
        @Param("operatorId") Long operatorId,
        @Param("gameProviderId") String gameProviderId,
        @Param("gameId") String gameId,
        @Param("now") LocalDateTime now);
}
