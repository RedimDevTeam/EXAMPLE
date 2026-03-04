package com.b2bplatform.b2c.repository;

import com.b2bplatform.b2c.model.ProviderTransaction;
import com.b2bplatform.b2c.model.ProviderTransaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProviderTransaction entity
 */
@Repository
public interface ProviderTransactionRepository extends JpaRepository<ProviderTransaction, Long> {
    
    /**
     * Find transaction by provider ID and transaction ID (for idempotency)
     */
    Optional<ProviderTransaction> findByProviderIdAndTransactionId(String providerId, String transactionId);
    
    /**
     * Find transactions by provider ID and player ID
     */
    List<ProviderTransaction> findByProviderIdAndPlayerIdOrderByCreatedAtDesc(String providerId, String playerId);
    
    /**
     * Find transactions by provider ID and status
     */
    List<ProviderTransaction> findByProviderIdAndStatus(String providerId, TransactionStatus status);
    
    /**
     * Find pending transactions for a provider
     */
    @Query("SELECT pt FROM ProviderTransaction pt WHERE pt.providerId = :providerId AND pt.status = 'PENDING' ORDER BY pt.createdAt ASC")
    List<ProviderTransaction> findPendingTransactionsByProvider(@Param("providerId") String providerId);
    
    /**
     * Find failed transactions that can be retried
     */
    @Query("SELECT pt FROM ProviderTransaction pt WHERE pt.providerId = :providerId AND pt.status = 'FAILED' AND pt.retryCount < :maxRetries ORDER BY pt.createdAt ASC")
    List<ProviderTransaction> findRetryableTransactions(@Param("providerId") String providerId, @Param("maxRetries") Integer maxRetries);
}
