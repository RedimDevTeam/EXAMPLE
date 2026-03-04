package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorCurrencyRepository extends JpaRepository<OperatorCurrency, Long> {
    
    /**
     * Find all currencies for an operator.
     */
    List<OperatorCurrency> findByOperatorIdOrderByIsDefaultDescCurrencyCodeAsc(Long operatorId);
    
    /**
     * Find active currencies for an operator.
     */
    List<OperatorCurrency> findByOperatorIdAndIsActiveTrueOrderByIsDefaultDescCurrencyCodeAsc(Long operatorId);
    
    /**
     * Find a specific currency for an operator.
     */
    Optional<OperatorCurrency> findByOperatorIdAndCurrencyCode(Long operatorId, String currencyCode);
    
    /**
     * Find the default currency for an operator.
     */
    Optional<OperatorCurrency> findByOperatorIdAndIsDefaultTrue(Long operatorId);
    
    /**
     * Check if operator has a default currency.
     */
    @Query("SELECT COUNT(oc) > 0 FROM OperatorCurrency oc WHERE oc.operatorId = :operatorId AND oc.isDefault = true")
    boolean hasDefaultCurrency(@Param("operatorId") Long operatorId);
    
    /**
     * Count active currencies for an operator.
     */
    long countByOperatorIdAndIsActiveTrue(Long operatorId);
    
    /**
     * Check if currency exists for operator.
     */
    boolean existsByOperatorIdAndCurrencyCode(Long operatorId, String currencyCode);
}
