package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.GameBetLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameBetLimitRepository extends JpaRepository<GameBetLimit, Long> {
    
    /**
     * Find active bet limit for a game and currency.
     * Active = isActive = true AND (effectiveTo IS NULL OR effectiveTo > NOW)
     */
    @Query("SELECT gbl FROM GameBetLimit gbl " +
           "WHERE gbl.gameId = :gameId " +
           "AND gbl.currencyCode = :currencyCode " +
           "AND gbl.isActive = true " +
           "AND (gbl.effectiveTo IS NULL OR gbl.effectiveTo > :now) " +
           "ORDER BY gbl.effectiveFrom DESC")
    Optional<GameBetLimit> findActiveByGameAndCurrency(
        @Param("gameId") String gameId,
        @Param("currencyCode") String currencyCode,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all active bet limits for a game (all currencies).
     */
    @Query("SELECT gbl FROM GameBetLimit gbl " +
           "WHERE gbl.gameId = :gameId " +
           "AND gbl.isActive = true " +
           "AND (gbl.effectiveTo IS NULL OR gbl.effectiveTo > :now) " +
           "ORDER BY gbl.currencyCode, gbl.effectiveFrom DESC")
    List<GameBetLimit> findActiveByGame(
        @Param("gameId") String gameId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all bet limits for a game (including inactive).
     */
    List<GameBetLimit> findByGameIdOrderByCurrencyCodeAscEffectiveFromDesc(String gameId);
    
    /**
     * Find all bet limits for a game and currency (including inactive).
     */
    List<GameBetLimit> findByGameIdAndCurrencyCodeOrderByEffectiveFromDesc(
        String gameId, 
        String currencyCode
    );
    
    /**
     * Check if there's an overlapping active limit for the same game and currency.
     */
    @Query("SELECT COUNT(gbl) > 0 FROM GameBetLimit gbl " +
           "WHERE gbl.gameId = :gameId " +
           "AND gbl.currencyCode = :currencyCode " +
           "AND gbl.isActive = true " +
           "AND gbl.effectiveFrom = :effectiveFrom " +
           "AND gbl.id != :excludeId")
    boolean existsOverlappingActiveLimit(
        @Param("gameId") String gameId,
        @Param("currencyCode") String currencyCode,
        @Param("effectiveFrom") LocalDateTime effectiveFrom,
        @Param("excludeId") Long excludeId
    );
}
