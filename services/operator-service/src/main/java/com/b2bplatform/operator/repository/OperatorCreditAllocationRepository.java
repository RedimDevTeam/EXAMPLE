package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorCreditAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorCreditAllocationRepository extends JpaRepository<OperatorCreditAllocation, Long> {
    
    List<OperatorCreditAllocation> findByParentOperatorIdAndIsActiveTrue(Long parentOperatorId);
    
    List<OperatorCreditAllocation> findByChildOperatorIdAndIsActiveTrue(Long childOperatorId);
    
    Optional<OperatorCreditAllocation> findByParentOperatorIdAndChildOperatorIdAndCurrencyCodeAndIsActiveTrue(
        Long parentOperatorId, Long childOperatorId, String currencyCode);
    
    List<OperatorCreditAllocation> findByParentOperatorIdAndCurrencyCodeAndIsActiveTrue(
        Long parentOperatorId, String currencyCode);
    
    List<OperatorCreditAllocation> findByChildOperatorIdAndCurrencyCodeAndIsActiveTrue(
        Long childOperatorId, String currencyCode);
    
    boolean existsByParentOperatorIdAndChildOperatorIdAndCurrencyCode(
        Long parentOperatorId, Long childOperatorId, String currencyCode);
}
