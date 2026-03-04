package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.BetLimitType;
import com.b2bplatform.operator.model.OperatorBetLimitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorBetLimitTypeRepository extends JpaRepository<OperatorBetLimitType, Long> {
    
    /**
     * Find all bet limit types for an operator, game, and currency.
     */
    List<OperatorBetLimitType> findByOperatorIdAndGameIdAndCurrencyCodeOrderByLimitTypeAsc(
        Long operatorId, String gameId, String currencyCode);
    
    /**
     * Find active bet limit types for an operator, game, and currency.
     */
    List<OperatorBetLimitType> findByOperatorIdAndGameIdAndCurrencyCodeAndIsActiveTrueOrderByLimitTypeAsc(
        Long operatorId, String gameId, String currencyCode);
    
    /**
     * Find a specific bet limit type.
     */
    Optional<OperatorBetLimitType> findByOperatorIdAndGameIdAndCurrencyCodeAndLimitType(
        Long operatorId, String gameId, String currencyCode, BetLimitType limitType);
    
    /**
     * Find all bet limit types for an operator and game (all currencies).
     */
    List<OperatorBetLimitType> findByOperatorIdAndGameIdOrderByCurrencyCodeAscLimitTypeAsc(
        Long operatorId, String gameId);
    
    /**
     * Find all bet limit types for an operator (all games).
     */
    List<OperatorBetLimitType> findByOperatorIdOrderByGameIdAscCurrencyCodeAscLimitTypeAsc(Long operatorId);
    
    /**
     * Check if bet limit type exists.
     */
    boolean existsByOperatorIdAndGameIdAndCurrencyCodeAndLimitType(
        Long operatorId, String gameId, String currencyCode, BetLimitType limitType);
}
