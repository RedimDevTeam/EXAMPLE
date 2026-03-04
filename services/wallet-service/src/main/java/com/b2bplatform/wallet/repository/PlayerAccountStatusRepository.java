package com.b2bplatform.wallet.repository;

import com.b2bplatform.wallet.model.PlayerAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Player Account Status
 */
@Repository
public interface PlayerAccountStatusRepository extends JpaRepository<PlayerAccountStatus, Long> {
    
    /**
     * Find by operator and player
     */
    Optional<PlayerAccountStatus> findByOperatorIdAndPlayerId(Long operatorId, String playerId);
    
    /**
     * Check if player is blocked
     */
    boolean existsByOperatorIdAndPlayerIdAndIsBlockedTrue(Long operatorId, String playerId);
}
