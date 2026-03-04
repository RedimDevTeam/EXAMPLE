package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorChipDenomination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorChipDenominationRepository extends JpaRepository<OperatorChipDenomination, Long> {
    
    /**
     * Find all chip denominations for an operator, game, and currency.
     * Ordered by chip index.
     */
    List<OperatorChipDenomination> findByOperatorIdAndGameIdAndCurrencyCodeOrderByChipIndexAsc(
        Long operatorId, String gameId, String currencyCode);
    
    /**
     * Find active chip denominations for an operator, game, and currency.
     * Ordered by display order (for UI).
     */
    List<OperatorChipDenomination> findByOperatorIdAndGameIdAndCurrencyCodeAndIsActiveTrueOrderByDisplayOrderAsc(
        Long operatorId, String gameId, String currencyCode);
    
    /**
     * Find a specific chip denomination by index.
     */
    Optional<OperatorChipDenomination> findByOperatorIdAndGameIdAndCurrencyCodeAndChipIndex(
        Long operatorId, String gameId, String currencyCode, Integer chipIndex);
    
    /**
     * Find all chip denominations for an operator and game (all currencies).
     */
    List<OperatorChipDenomination> findByOperatorIdAndGameIdOrderByCurrencyCodeAscChipIndexAsc(
        Long operatorId, String gameId);
    
    /**
     * Find all chip denominations for an operator (all games).
     */
    List<OperatorChipDenomination> findByOperatorIdOrderByGameIdAscCurrencyCodeAscChipIndexAsc(Long operatorId);
    
    /**
     * Check if chip denomination exists.
     */
    boolean existsByOperatorIdAndGameIdAndCurrencyCodeAndChipIndex(
        Long operatorId, String gameId, String currencyCode, Integer chipIndex);
    
    /**
     * Count active chip denominations for an operator, game, and currency.
     */
    long countByOperatorIdAndGameIdAndCurrencyCodeAndIsActiveTrue(
        Long operatorId, String gameId, String currencyCode);
}
