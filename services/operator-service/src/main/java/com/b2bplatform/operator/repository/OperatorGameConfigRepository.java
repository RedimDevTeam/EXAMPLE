package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorGameConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorGameConfigRepository extends JpaRepository<OperatorGameConfig, Long> {
    
    /**
     * Find active game configuration for an operator and game.
     * Active = isActive = true AND (effectiveTo IS NULL OR effectiveTo > NOW)
     */
    @Query("SELECT ogc FROM OperatorGameConfig ogc " +
           "WHERE ogc.operatorId = :operatorId " +
           "AND ogc.gameProviderId = :gameProviderId " +
           "AND ogc.gameId = :gameId " +
           "AND ogc.isActive = true " +
           "AND (ogc.effectiveTo IS NULL OR ogc.effectiveTo > :now) " +
           "ORDER BY ogc.effectiveFrom DESC")
    Optional<OperatorGameConfig> findActiveByOperatorAndGame(
        @Param("operatorId") Long operatorId,
        @Param("gameProviderId") String gameProviderId,
        @Param("gameId") String gameId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all active game configurations for an operator.
     */
    @Query("SELECT ogc FROM OperatorGameConfig ogc " +
           "WHERE ogc.operatorId = :operatorId " +
           "AND ogc.isActive = true " +
           "AND (ogc.effectiveTo IS NULL OR ogc.effectiveTo > :now) " +
           "ORDER BY ogc.gameProviderId, ogc.gameId, ogc.effectiveFrom DESC")
    List<OperatorGameConfig> findActiveByOperator(
        @Param("operatorId") Long operatorId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all active game configurations for an operator and provider.
     */
    @Query("SELECT ogc FROM OperatorGameConfig ogc " +
           "WHERE ogc.operatorId = :operatorId " +
           "AND ogc.gameProviderId = :gameProviderId " +
           "AND ogc.isActive = true " +
           "AND (ogc.effectiveTo IS NULL OR ogc.effectiveTo > :now) " +
           "ORDER BY ogc.gameId, ogc.effectiveFrom DESC")
    List<OperatorGameConfig> findActiveByOperatorAndProvider(
        @Param("operatorId") Long operatorId,
        @Param("gameProviderId") String gameProviderId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all enabled games for an operator (for game listing).
     */
    @Query("SELECT ogc FROM OperatorGameConfig ogc " +
           "WHERE ogc.operatorId = :operatorId " +
           "AND ogc.isEnabled = true " +
           "AND ogc.isActive = true " +
           "AND (ogc.effectiveTo IS NULL OR ogc.effectiveTo > :now) " +
           "ORDER BY ogc.gameProviderId, ogc.gameId")
    List<OperatorGameConfig> findEnabledGamesForOperator(
        @Param("operatorId") Long operatorId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all game configurations for an operator (including inactive).
     */
    List<OperatorGameConfig> findByOperatorIdOrderByGameProviderIdAscGameIdAscEffectiveFromDesc(Long operatorId);
    
    /**
     * Find all game configurations for an operator and provider (including inactive).
     */
    List<OperatorGameConfig> findByOperatorIdAndGameProviderIdOrderByGameIdAscEffectiveFromDesc(
        Long operatorId, 
        String gameProviderId
    );
    
    /**
     * Check if there's an overlapping active config for the same operator, provider, and game.
     */
    @Query("SELECT COUNT(ogc) > 0 FROM OperatorGameConfig ogc " +
           "WHERE ogc.operatorId = :operatorId " +
           "AND ogc.gameProviderId = :gameProviderId " +
           "AND ogc.gameId = :gameId " +
           "AND ogc.isActive = true " +
           "AND ogc.effectiveFrom = :effectiveFrom " +
           "AND ogc.id != :excludeId")
    boolean existsOverlappingActiveConfig(
        @Param("operatorId") Long operatorId,
        @Param("gameProviderId") String gameProviderId,
        @Param("gameId") String gameId,
        @Param("effectiveFrom") LocalDateTime effectiveFrom,
        @Param("excludeId") Long excludeId
    );
}
