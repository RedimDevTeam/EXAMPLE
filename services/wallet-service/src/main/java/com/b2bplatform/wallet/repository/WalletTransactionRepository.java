package com.b2bplatform.wallet.repository;

import com.b2bplatform.wallet.model.TransactionType;
import com.b2bplatform.wallet.model.WalletTransaction;
import com.b2bplatform.wallet.model.WalletTransaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    /**
     * Find transaction by transaction ID
     */
    Optional<WalletTransaction> findByTransactionId(String transactionId);
    
    /**
     * Find transactions by player ID, ordered by creation date descending
     */
    List<WalletTransaction> findByPlayerIdOrderByCreatedAtDesc(String playerId);
    
    /**
     * Find transactions by player ID and status (legacy - using String)
     */
    @Deprecated
    List<WalletTransaction> findByPlayerIdAndStatus(String playerId, String status);
    
    /**
     * Find successful transactions by player (legacy query)
     */
    @Deprecated
    @Query("SELECT t FROM WalletTransaction t WHERE t.playerId = :playerId AND t.status = 'SUCCESS' ORDER BY t.createdAt DESC")
    List<WalletTransaction> findSuccessfulTransactionsByPlayer(@Param("playerId") String playerId);
    
    /**
     * Find transactions by wallet ID
     */
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    /**
     * Find transactions by reference (for linking related transactions)
     */
    List<WalletTransaction> findByReference(String reference);
    
    /**
     * Find transactions by related transaction ID
     */
    List<WalletTransaction> findByRelatedTransactionId(Long relatedTransactionId);
    
    /**
     * Find transactions by type and status
     */
    List<WalletTransaction> findByTransactionTypeAndStatus(
        TransactionType transactionType, 
        TransactionStatus status
    );
    
    /**
     * Find transactions by wallet ID and date range
     */
    @Query("SELECT t FROM WalletTransaction t WHERE t.wallet.id = :walletId " +
           "AND t.createdAt >= :startDate AND t.createdAt <= :endDate " +
           "ORDER BY t.createdAt DESC")
    List<WalletTransaction> findByWalletIdAndDateRange(
        @Param("walletId") Long walletId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find last transaction for a wallet (for balance verification)
     */
    @Query("SELECT t FROM WalletTransaction t WHERE t.wallet.id = :walletId " +
           "AND t.status = :status ORDER BY t.createdAt DESC")
    Optional<WalletTransaction> findLastTransactionByWalletId(
        @Param("walletId") Long walletId,
        @Param("status") TransactionStatus status
    );
}
