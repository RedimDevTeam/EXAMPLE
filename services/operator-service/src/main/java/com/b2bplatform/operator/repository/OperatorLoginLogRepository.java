package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for OperatorLoginLog entity.
 */
@Repository
public interface OperatorLoginLogRepository extends JpaRepository<OperatorLoginLog, Long> {
    
    /**
     * Find login logs by username.
     */
    Page<OperatorLoginLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    
    /**
     * Find login logs by login status.
     */
    Page<OperatorLoginLog> findByLoginStatusOrderByCreatedAtDesc(String loginStatus, Pageable pageable);
    
    /**
     * Find login logs by username and status.
     */
    Page<OperatorLoginLog> findByUsernameAndLoginStatusOrderByCreatedAtDesc(
        String username, String loginStatus, Pageable pageable);
    
    /**
     * Find login logs by IP address.
     */
    Page<OperatorLoginLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);
    
    /**
     * Find login logs within date range.
     */
    @Query("SELECT l FROM OperatorLoginLog l WHERE l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    Page<OperatorLoginLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    /**
     * Find failed login attempts for a username within time window.
     */
    @Query("SELECT l FROM OperatorLoginLog l WHERE l.username = :username AND l.loginStatus = 'FAILED' AND l.createdAt >= :since ORDER BY l.createdAt DESC")
    List<OperatorLoginLog> findFailedAttemptsSince(
        @Param("username") String username,
        @Param("since") LocalDateTime since);
    
    /**
     * Count failed login attempts for a username within time window.
     */
    @Query("SELECT COUNT(l) FROM OperatorLoginLog l WHERE l.username = :username AND l.loginStatus = 'FAILED' AND l.createdAt >= :since")
    long countFailedAttemptsSince(
        @Param("username") String username,
        @Param("since") LocalDateTime since);
}
