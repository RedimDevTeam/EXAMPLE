package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorRevenueSharing;
import com.b2bplatform.operator.model.RevenueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorRevenueSharingRepository extends JpaRepository<OperatorRevenueSharing, Long> {
    
    List<OperatorRevenueSharing> findByOperatorIdAndIsActiveTrue(Long operatorId);
    
    List<OperatorRevenueSharing> findByParentOperatorIdAndIsActiveTrue(Long parentOperatorId);
    
    List<OperatorRevenueSharing> findByOperatorIdAndRevenueTypeAndIsActiveTrue(Long operatorId, RevenueType revenueType);
    
    @Query("SELECT rs FROM OperatorRevenueSharing rs WHERE rs.operatorId = :operatorId " +
           "AND rs.isActive = true " +
           "AND (rs.effectiveTo IS NULL OR rs.effectiveTo >= :currentDate) " +
           "AND rs.effectiveFrom <= :currentDate")
    List<OperatorRevenueSharing> findActiveRevenueSharing(
        @Param("operatorId") Long operatorId, 
        @Param("currentDate") LocalDateTime currentDate);
    
    Optional<OperatorRevenueSharing> findByOperatorIdAndParentOperatorIdAndRevenueTypeAndIsActiveTrue(
        Long operatorId, Long parentOperatorId, RevenueType revenueType);
}
