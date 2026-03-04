package com.b2bplatform.wallet.repository;

import com.b2bplatform.wallet.model.PendingFundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Pending Fund Transactions
 */
@Repository
public interface PendingFundTransactionRepository extends JpaRepository<PendingFundTransaction, Long> {
    
    /**
     * Find by payment ID
     */
    Optional<PendingFundTransaction> findByPaymentId(String paymentId);
    
    /**
     * Find by operator and player
     */
    List<PendingFundTransaction> findByOperatorIdAndPlayerId(Long operatorId, String playerId);
    
    /**
     * Find by status
     */
    List<PendingFundTransaction> findByStatus(PendingFundTransaction.PendingStatus status);
    
    /**
     * Find expired pending transactions
     */
    @Query("SELECT p FROM PendingFundTransaction p WHERE p.status = 'PENDING' AND p.expiresAt < :now")
    List<PendingFundTransaction> findExpiredPending(LocalDateTime now);
    
    /**
     * Find pending transactions for operator and player
     */
    List<PendingFundTransaction> findByOperatorIdAndPlayerIdAndStatus(
        Long operatorId, 
        String playerId, 
        PendingFundTransaction.PendingStatus status
    );
}
