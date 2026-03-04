package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorApiAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for OperatorApiAccessLog entity.
 */
@Repository
public interface OperatorApiAccessLogRepository extends JpaRepository<OperatorApiAccessLog, Long> {
    
    /**
     * Find API access logs by operator ID.
     */
    Page<OperatorApiAccessLog> findByOperatorIdOrderByCreatedAtDesc(Long operatorId, Pageable pageable);
    
    /**
     * Find API access logs by endpoint.
     */
    Page<OperatorApiAccessLog> findByEndpointOrderByCreatedAtDesc(String endpoint, Pageable pageable);
    
    /**
     * Find API access logs by HTTP status.
     */
    Page<OperatorApiAccessLog> findByHttpStatusOrderByCreatedAtDesc(Integer httpStatus, Pageable pageable);
    
    /**
     * Find API access logs by operator ID and endpoint.
     */
    Page<OperatorApiAccessLog> findByOperatorIdAndEndpointOrderByCreatedAtDesc(
        Long operatorId, String endpoint, Pageable pageable);
    
    /**
     * Find API access logs within date range.
     */
    @Query("SELECT a FROM OperatorApiAccessLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<OperatorApiAccessLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    /**
     * Find API access logs by operator ID within date range.
     */
    @Query("SELECT a FROM OperatorApiAccessLog a WHERE a.operatorId = :operatorId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<OperatorApiAccessLog> findByOperatorIdAndDateRange(
        @Param("operatorId") Long operatorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    /**
     * Count API access logs by operator ID.
     */
    long countByOperatorId(Long operatorId);
}
