package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorUrlConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for OperatorUrlConfig entity.
 */
@Repository
public interface OperatorUrlConfigRepository extends JpaRepository<OperatorUrlConfig, Long> {
    
    /**
     * Find URL config by operator ID.
     */
    Optional<OperatorUrlConfig> findByOperatorId(Long operatorId);
    
    /**
     * Check if URL config exists for operator.
     */
    boolean existsByOperatorId(Long operatorId);
}
