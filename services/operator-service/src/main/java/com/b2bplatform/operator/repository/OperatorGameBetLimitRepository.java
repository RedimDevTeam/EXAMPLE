package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorGameBetLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorGameBetLimitRepository extends JpaRepository<OperatorGameBetLimit, Long> {
    
    /**
     * Find active bet limit for an operator, game, and currency.
     * Active = isActive = true AND (effectiveTo IS NULL OR effectiveTo > NOW)
     */
    @Query("SELECT ogbl FROM OperatorGameBetLimit ogbl " +
           "WHERE ogbl.operatorId = :operatorId " +
           "AND ogbl.gameId = :gameId " +
           "AND ogbl.currencyCode = :currencyCode " +
           "AND ogbl.isActive = true " +
           "AND (ogbl.effectiveTo IS NULL OR ogbl.effectiveTo > :now) " +
           "ORDER BY ogbl.effectiveFrom DESC")
    Optional<OperatorGameBetLimit> findActiveByOperatorAndGameAndCurrency(
        @Param("operatorId") Long operatorId,
        @Param("gameId") String gameId,
        @Param("currencyCode") String currencyCode,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all active bet limits for an operator and game (all currencies).
     */
    @Query("SELECT ogbl FROM OperatorGameBetLimit ogbl " +
           "WHERE ogbl.operatorId = :operatorId " +
           "AND ogbl.gameId = :gameId " +
           "AND ogbl.isActive = true " +
           "AND (ogbl.effectiveTo IS NULL OR ogbl.effectiveTo > :now) " +
           "ORDER BY ogbl.currencyCode, ogbl.effectiveFrom DESC")
    List<OperatorGameBetLimit> findActiveByOperatorAndGame(
        @Param("operatorId") Long operatorId,
        @Param("gameId") String gameId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all bet limits for an operator and game (including inactive).
     */
    List<OperatorGameBetLimit> findByOperatorIdAndGameIdOrderByCurrencyCodeAscEffectiveFromDesc(
        Long operatorId, 
        String gameId
    );
    
    /**
     * Find all bet limits for an operator, game, and currency (including inactive).
     */
    List<OperatorGameBetLimit> findByOperatorIdAndGameIdAndCurrencyCodeOrderByEffectiveFromDesc(
        Long operatorId, 
        String gameId, 
        String currencyCode
    );
    
    /**
     * Find all active bet limits for an operator (all games).
     */
    @Query("SELECT ogbl FROM OperatorGameBetLimit ogbl " +
           "WHERE ogbl.operatorId = :operatorId " +
           "AND ogbl.isActive = true " +
           "AND (ogbl.effectiveTo IS NULL OR ogbl.effectiveTo > :now) " +
           "ORDER BY ogbl.gameId, ogbl.currencyCode, ogbl.effectiveFrom DESC")
    List<OperatorGameBetLimit> findActiveByOperator(
        @Param("operatorId") Long operatorId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Check if there's an overlapping active limit for the same operator, game, and currency.
     */
    @Query("SELECT COUNT(ogbl) > 0 FROM OperatorGameBetLimit ogbl " +
           "WHERE ogbl.operatorId = :operatorId " +
           "AND ogbl.gameId = :gameId " +
           "AND ogbl.currencyCode = :currencyCode " +
           "AND ogbl.isActive = true " +
           "AND ogbl.effectiveFrom = :effectiveFrom " +
           "AND ogbl.id != :excludeId")
    boolean existsOverlappingActiveLimit(
        @Param("operatorId") Long operatorId,
        @Param("gameId") String gameId,
        @Param("currencyCode") String currencyCode,
        @Param("effectiveFrom") LocalDateTime effectiveFrom,
        @Param("excludeId") Long excludeId
    );
}
