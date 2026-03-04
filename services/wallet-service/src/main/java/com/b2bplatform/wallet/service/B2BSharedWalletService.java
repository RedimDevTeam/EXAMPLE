package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.dto.b2b.B2BCancelRequest;
import com.b2bplatform.wallet.dto.b2b.B2BCreditRequest;
import com.b2bplatform.wallet.dto.b2b.B2BDebitRequest;
import com.b2bplatform.wallet.dto.b2b.B2BRefundRequest;
import com.b2bplatform.wallet.dto.response.BalanceResponse;
import com.b2bplatform.wallet.dto.response.CreditResponse;
import com.b2bplatform.wallet.dto.response.DebitResponse;
import com.b2bplatform.wallet.model.TransactionSubtype;
import com.b2bplatform.wallet.model.UnitType;
import com.b2bplatform.wallet.model.WalletTransaction;
import com.b2bplatform.wallet.repository.WalletTransactionRepository;
import com.b2bplatform.wallet.util.TransactionSubtypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * B2B Integration Service for Shared Wallet operations
 * Handles B2B API requests with industry-standard field naming
 * Maps B2B DTOs to internal DTOs and entities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class B2BSharedWalletService {
    
    private final WalletService walletService;
    private final OperatorServiceClient operatorServiceClient;
    private final WalletTransactionRepository transactionRepository;
    
    /**
     * Process B2B Debit Request
     * Converts operatorCode to operatorId and handles extended fields
     */
    @Transactional
    public DebitResponse debit(B2BDebitRequest request) {
        log.info("B2B Debit request - operatorCode: {}, playerId: {}, amount: {}, transactionId: {}", 
            request.getOperatorCode(), request.getPlayerId(), request.getAmount(), request.getTransactionId());
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(request.getOperatorCode());
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + request.getOperatorCode());
        }
        
        // Validate transaction subtype
        if (!request.isValidTransactionSubtype()) {
            throw new IllegalArgumentException("Invalid transaction subtype ID: " + request.getTransactionSubtypeId());
        }
        
        // Convert amount based on unitType
        BigDecimal amount = convertAmount(request.getAmount(), request.getUnitType());
        
        // Process debit using existing service
        DebitResponse response = walletService.debit(
            operatorId,
            request.getPlayerId(),
            amount,
            request.getCurrency(),
            request.getReference() != null ? request.getReference() : request.getTransactionId(),
            request.getDescription()
        );
        
        // Update transaction with extended fields
        updateTransactionWithExtendedFields(
            response.getTransactionId(),
            request.getPlayerLevel(),
            request.getUnitType() != null ? request.getUnitType().name() : null,
            request.getRoundId(),
            request.getGameId(),
            request.getHandId(),
            request.getTransactionSubtypeId(),
            request.getBrandId(),
            request.getAgentId(),
            request.getLanguage()
        );
        
        return response;
    }
    
    /**
     * Process B2B Credit Request
     * Converts operatorCode to operatorId and handles extended fields
     */
    @Transactional
    public CreditResponse credit(B2BCreditRequest request) {
        log.info("B2B Credit request - operatorCode: {}, playerId: {}, amount: {}, transactionId: {}", 
            request.getOperatorCode(), request.getPlayerId(), request.getAmount(), request.getTransactionId());
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(request.getOperatorCode());
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + request.getOperatorCode());
        }
        
        // Validate transaction subtype
        if (!request.isValidTransactionSubtype()) {
            throw new IllegalArgumentException("Invalid transaction subtype ID: " + request.getTransactionSubtypeId());
        }
        
        // Convert amount based on unitType
        BigDecimal amount = convertAmount(request.getAmount(), request.getUnitType());
        
        // Process credit using existing service
        CreditResponse response = walletService.credit(
            operatorId,
            request.getPlayerId(),
            amount,
            request.getCurrency(),
            request.getReference() != null ? request.getReference() : request.getTransactionId(),
            request.getDescription()
        );
        
        // Update transaction with extended fields
        updateTransactionWithExtendedFields(
            response.getTransactionId(),
            request.getPlayerLevel(),
            request.getUnitType() != null ? request.getUnitType().name() : null,
            request.getRoundId(),
            request.getGameId(),
            request.getHandId(),
            request.getTransactionSubtypeId(),
            request.getBrandId(),
            request.getAgentId(),
            request.getLanguage()
        );
        
        return response;
    }
    
    /**
     * Process B2B Refund Request
     */
    @Transactional
    public CreditResponse refund(B2BRefundRequest request) {
        log.info("B2B Refund request - operatorCode: {}, playerId: {}, amount: {}, transactionId: {}", 
            request.getOperatorCode(), request.getPlayerId(), request.getAmount(), request.getTransactionId());
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(request.getOperatorCode());
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + request.getOperatorCode());
        }
        
        // Find original transaction
        Optional<WalletTransaction> originalTransaction = transactionRepository
            .findByTransactionId(request.getOriginalTransactionId());
        
        if (originalTransaction.isEmpty()) {
            throw new IllegalArgumentException("Original transaction not found: " + request.getOriginalTransactionId());
        }
        
        // Convert amount based on unitType
        BigDecimal amount = convertAmount(request.getAmount(), request.getUnitType());
        
        // Process refund (credit) using existing service
        CreditResponse response = walletService.credit(
            operatorId,
            request.getPlayerId(),
            amount,
            request.getCurrency(),
            request.getTransactionId(),
            request.getDescription() != null ? request.getDescription() : "Refund for " + request.getOriginalTransactionId()
        );
        
        // Link refund to original transaction
        WalletTransaction refundTransaction = transactionRepository
            .findByTransactionId(response.getTransactionId())
            .orElseThrow(() -> new IllegalStateException("Refund transaction not found after creation"));
        
        refundTransaction.setRelatedTransaction(originalTransaction.get());
        transactionRepository.save(refundTransaction);
        
        // Update transaction with extended fields
        updateTransactionWithExtendedFields(
            response.getTransactionId(),
            null, // playerLevel
            request.getUnitType() != null ? request.getUnitType().name() : null,
            request.getRoundId(),
            request.getGameId(),
            null, // handId
            request.getTransactionSubtypeId() != null ? request.getTransactionSubtypeId() : TransactionSubtype.REFUND.getCode(),
            null, // brandId
            null, // agentId
            null  // language
        );
        
        return response;
    }
    
    /**
     * Process B2B Cancel Request
     */
    @Transactional
    public void cancel(B2BCancelRequest request) {
        log.info("B2B Cancel request - operatorCode: {}, playerId: {}, transactionId: {}", 
            request.getOperatorCode(), request.getPlayerId(), request.getTransactionId());
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(request.getOperatorCode());
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + request.getOperatorCode());
        }
        
        // Find transaction
        Optional<WalletTransaction> transactionOpt = transactionRepository
            .findByTransactionId(request.getTransactionId());
        
        if (transactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found: " + request.getTransactionId());
        }
        
        WalletTransaction transaction = transactionOpt.get();
        
        // Validate operator matches
        if (!transaction.getOperatorId().equals(operatorId)) {
            throw new IllegalArgumentException("Transaction does not belong to operator: " + request.getOperatorCode());
        }
        
        // Validate player matches
        if (!transaction.getPlayerId().equals(request.getPlayerId())) {
            throw new IllegalArgumentException("Transaction does not belong to player: " + request.getPlayerId());
        }
        
        // Only cancel if status is PENDING
        if (transaction.getStatus() != WalletTransaction.TransactionStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel transaction with status: " + transaction.getStatus());
        }
        
        // Update status to CANCELLED
        transaction.setStatus(WalletTransaction.TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
        
        log.info("Transaction cancelled - transactionId: {}", request.getTransactionId());
    }
    
    /**
     * Get balance (B2B API)
     */
    public BalanceResponse getBalance(String operatorCode, String playerId) {
        log.info("B2B Balance query - operatorCode: {}, playerId: {}", operatorCode, playerId);
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(operatorCode);
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + operatorCode);
        }
        
        return walletService.getBalance(operatorId, playerId);
    }
    
    // ============================================================================
    // Helper Methods
    // ============================================================================
    
    /**
     * Convert amount based on unitType
     */
    private BigDecimal convertAmount(BigDecimal amount, UnitType unitType) {
        if (amount == null) {
            return null;
        }
        
        if (unitType == null || unitType == UnitType.DECIMAL) {
            return amount; // Already in decimal format
        } else if (unitType == UnitType.CENTS) {
            return UnitType.centsToDecimal(amount); // Convert cents to decimal
        }
        
        return amount; // Default to decimal
    }
    
    /**
     * Update transaction with extended fields
     */
    private void updateTransactionWithExtendedFields(
            String transactionId,
            Integer playerLevel,
            String unitType,
            String roundId,
            String gameId,
            String handId,
            Integer transactionSubtypeId,
            String brandId,
            String agentId,
            String language) {
        
        Optional<WalletTransaction> transactionOpt = transactionRepository
            .findByTransactionId(transactionId);
        
        if (transactionOpt.isPresent()) {
            WalletTransaction transaction = transactionOpt.get();
            transaction.setPlayerLevel(playerLevel);
            transaction.setUnitType(unitType);
            transaction.setRoundId(roundId);
            transaction.setGameId(gameId);
            transaction.setHandId(handId);
            transaction.setTransactionSubtypeId(transactionSubtypeId);
            transaction.setBrandId(brandId);
            transaction.setAgentId(agentId);
            transaction.setLanguage(language);
            transactionRepository.save(transaction);
            
            log.debug("Updated transaction {} with extended fields", transactionId);
        } else {
            log.warn("Transaction not found for updating extended fields: {}", transactionId);
        }
    }
}
