package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorCommissionCalculation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for OperatorCommissionCalculation entity.
 */
@Repository
public interface OperatorCommissionCalculationRepository extends JpaRepository<OperatorCommissionCalculation, Long> {
    
    /**
     * Find calculations by operator ID.
     */
    Page<OperatorCommissionCalculation> findByOperatorIdOrderByCalculatedAtDesc(
        Long operatorId, Pageable pageable);
    
    /**
     * Find calculations by operator ID and game provider.
     */
    Page<OperatorCommissionCalculation> findByOperatorIdAndGameProviderIdOrderByCalculatedAtDesc(
        Long operatorId, String gameProviderId, Pageable pageable);
    
    /**
     * Find calculations within date range.
     */
    @Query("SELECT c FROM OperatorCommissionCalculation c WHERE " +
           "c.operatorId = :operatorId AND " +
           "c.calculationPeriodStart >= :startDate AND " +
           "c.calculationPeriodEnd <= :endDate " +
           "ORDER BY c.calculatedAt DESC")
    Page<OperatorCommissionCalculation> findByOperatorIdAndDateRange(
        @Param("operatorId") Long operatorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    /**
     * Find calculations by settlement cycle ID.
     */
    List<OperatorCommissionCalculation> findBySettlementCycleId(Long settlementCycleId);
}
