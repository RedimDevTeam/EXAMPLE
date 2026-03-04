package com.b2bplatform.b2c.repository;

import com.b2bplatform.b2c.model.ProviderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProviderConfig entity
 */
@Repository
public interface ProviderConfigRepository extends JpaRepository<ProviderConfig, Long> {
    
    /**
     * Find provider config by provider ID
     */
    Optional<ProviderConfig> findByProviderId(String providerId);
    
    /**
     * Find all active providers
     */
    List<ProviderConfig> findByIsActiveTrue();
    
    /**
     * Check if provider ID exists
     */
    boolean existsByProviderId(String providerId);
}
