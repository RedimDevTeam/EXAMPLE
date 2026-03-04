package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for OperatorAuditLog entity.
 */
@Repository
public interface OperatorAuditLogRepository extends JpaRepository<OperatorAuditLog, Long> {
    
    /**
     * Find audit logs by operator ID.
     */
    Page<OperatorAuditLog> findByOperatorIdOrderByCreatedAtDesc(Long operatorId, Pageable pageable);
    
    /**
     * Find audit logs by action type.
     */
    Page<OperatorAuditLog> findByActionTypeOrderByCreatedAtDesc(String actionType, Pageable pageable);
    
    /**
     * Find audit logs by operator ID and action type.
     */
    Page<OperatorAuditLog> findByOperatorIdAndActionTypeOrderByCreatedAtDesc(
        Long operatorId, String actionType, Pageable pageable);
    
    /**
     * Find audit logs by performed by (username).
     */
    Page<OperatorAuditLog> findByPerformedByOrderByCreatedAtDesc(String performedBy, Pageable pageable);
    
    /**
     * Find audit logs within date range.
     */
    @Query("SELECT a FROM OperatorAuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<OperatorAuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    /**
     * Find audit logs by operator ID within date range.
     */
    @Query("SELECT a FROM OperatorAuditLog a WHERE a.operatorId = :operatorId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<OperatorAuditLog> findByOperatorIdAndDateRange(
        @Param("operatorId") Long operatorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    /**
     * Count audit logs by operator ID.
     */
    long countByOperatorId(Long operatorId);
}
