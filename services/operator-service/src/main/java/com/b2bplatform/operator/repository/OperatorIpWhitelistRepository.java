package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorIpWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for OperatorIpWhitelist entity.
 */
@Repository
public interface OperatorIpWhitelistRepository extends JpaRepository<OperatorIpWhitelist, Long> {
    
    /**
     * Find all active IP whitelist entries for an operator.
     */
    List<OperatorIpWhitelist> findByOperatorIdAndIsActiveTrue(Long operatorId);
    
    /**
     * Find all IP whitelist entries for an operator (including inactive).
     */
    List<OperatorIpWhitelist> findByOperatorId(Long operatorId);
    
    /**
     * Find IP whitelist entry by operator ID and IP address.
     */
    Optional<OperatorIpWhitelist> findByOperatorIdAndIpAddress(Long operatorId, String ipAddress);
    
    /**
     * Check if IP address exists for operator.
     */
    boolean existsByOperatorIdAndIpAddress(Long operatorId, String ipAddress);
    
    /**
     * Find all active IP whitelist entries matching an IP address (across all operators).
     */
    List<OperatorIpWhitelist> findByIpAddressAndIsActiveTrue(String ipAddress);
}
