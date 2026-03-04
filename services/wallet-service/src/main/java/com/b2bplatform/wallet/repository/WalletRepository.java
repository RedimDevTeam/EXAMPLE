package com.b2bplatform.wallet.repository;

import com.b2bplatform.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Repository for Wallet entity with pessimistic locking support.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    /**
     * Find wallet by operator and player ID
     */
    Optional<Wallet> findByOperatorIdAndPlayerId(Long operatorId, String playerId);
    
    /**
     * Find wallet by operator and player ID with pessimistic lock (for concurrent transactions)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.operatorId = :operatorId AND w.playerId = :playerId")
    Optional<Wallet> findByOperatorIdAndPlayerIdWithLock(
        @Param("operatorId") Long operatorId, 
        @Param("playerId") String playerId
    );
    
    /**
     * Check if wallet exists
     */
    boolean existsByOperatorIdAndPlayerId(Long operatorId, String playerId);
}
