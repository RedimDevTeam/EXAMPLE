package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.model.OperatorWalletConfig;
import com.b2bplatform.wallet.model.TransactionType;
import com.b2bplatform.wallet.model.Wallet;
import com.b2bplatform.wallet.model.Wallet.WalletModel;
import com.b2bplatform.wallet.model.WalletTransaction;
import com.b2bplatform.wallet.model.WalletTransaction.TransactionStatus;
import com.b2bplatform.wallet.repository.WalletRepository;
import com.b2bplatform.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Enhanced wallet transaction service with balance tracking and audit trail.
 * Supports both Shared Wallet and Fund Transfer models.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedWalletTransactionService {
    
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletManagementService walletManagementService;
    private final OperatorWebhookClient webhookClient;
    private final OperatorWalletConfigCacheService configCacheService;
    
    // ObjectMapper for JSON logging (static final - thread-safe, shared across instances)
    private static final ObjectMapper objectMapper;
    
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        objectMapper = mapper;
    }
    
    /**
     * Process debit transaction with balance tracking
     * 
     * @param operatorId Operator ID
     * @param playerId Player ID
     * @param amount Amount to debit
     * @param currency Currency
     * @param reference External reference (e.g., bet ID)
     * @param description Transaction description
     * @param transactionType Transaction type (BET, WITHDRAWAL, TRANSFER_OUT)
     * @param walletModel Wallet model (SHARED_WALLET or FUND_TRANSFER)
     * @param relatedTransactionId Related transaction ID (for linking)
     * @return Transaction response
     */
    @Transactional
    public Map<String, Object> processDebit(
            Long operatorId, 
            String playerId, 
            BigDecimal amount,
            String currency,
            String reference,
            String description,
            TransactionType transactionType,
            WalletModel walletModel,
            Long relatedTransactionId) {
        
        log.info("Processing debit - operator: {}, player: {}, amount: {}, type: {}, model: {}", 
            operatorId, playerId, amount, transactionType, walletModel);
        
        // Get or create wallet
        Wallet wallet = walletManagementService.getOrCreateWallet(
            operatorId, playerId, walletModel, currency);
        
        // Lock wallet for concurrent operations
        wallet = walletManagementService.getWalletWithLock(operatorId, playerId);
        
        // Validate wallet
        walletManagementService.validateWalletStatus(wallet);
        walletManagementService.validateCurrency(wallet, currency);
        
        // Record balance before (for Fund Transfer model)
        BigDecimal balanceBefore = wallet.getBalance();
        
        // For Fund Transfer model, validate balance
        if (walletModel == WalletModel.FUND_TRANSFER) {
            walletManagementService.validateBalance(wallet, amount);
        }
        
        // Generate transaction ID
        String transactionId = generateTransactionId();
        
        // Check idempotency - CRITICAL: Use findByReference, not findByTransactionId
        // This prevents duplicate debits for the same bet
        if (reference != null && !reference.isEmpty()) {
            List<WalletTransaction> existing = transactionRepository.findByReference(reference);
            Optional<WalletTransaction> completed = existing.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .findFirst();
            if (completed.isPresent()) {
                log.warn("Duplicate debit transaction detected (idempotency) - reference: {}, returning existing transaction. This prevents duplicate bet posts.", reference);
                return buildSuccessResponse(completed.get());
            }
        }
        
        // Get operator wallet config
        OperatorWalletConfig config = configCacheService.getConfig(operatorId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Operator wallet config not found or disabled: " + operatorId));
        
        // Build request payload
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("playerId", playerId);
        requestPayload.put("amount", amount);
        requestPayload.put("currency", currency);
        requestPayload.put("transactionId", transactionId);
        requestPayload.put("reference", reference);
        requestPayload.put("description", description);
        
        // Log request payload (for audit trail - not stored in DB)
        try {
            log.info("Wallet transaction request - transactionId: {}, type: {}, payload: {}", 
                transactionId, transactionType, objectMapper.writeValueAsString(requestPayload));
        } catch (Exception e) {
            log.warn("Failed to serialize request payload for logging - transactionId: {}", transactionId, e);
        }
        
        // Calculate balance after (for Fund Transfer model)
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        
        // Create transaction record (without payloads - they're logged instead)
        // Note: operator_url not stored - can be retrieved from operator_wallet_config table
        WalletTransaction transaction = WalletTransaction.builder()
            .wallet(wallet)
            .operatorId(operatorId)
            .playerId(playerId)
            .transactionId(transactionId)
            .transactionType(transactionType)
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(walletModel == WalletModel.FUND_TRANSFER ? balanceAfter : null)
            .currency(currency)
            .reference(reference)
            .description(description)
            .status(TransactionStatus.PENDING)
            .build();
        
        // Link to related transaction if provided
        if (relatedTransactionId != null) {
            Optional<WalletTransaction> related = transactionRepository.findById(relatedTransactionId);
            related.ifPresent(transaction::setRelatedTransaction);
        }
        
        transaction = transactionRepository.save(transaction);
        
        try {
            // Call operator webhook (for Shared Wallet model or Fund Transfer TRANSFER_OUT)
            Map<String, Object> response = null;
            if (walletModel == WalletModel.SHARED_WALLET || 
                (walletModel == WalletModel.FUND_TRANSFER && transactionType == TransactionType.TRANSFER_OUT)) {
                response = webhookClient.debit(config, requestPayload).block();
            }
            
            // Log response payload (for audit trail - not stored in DB)
            if (response != null) {
                try {
                    log.info("Wallet transaction response - transactionId: {}, type: {}, payload: {}", 
                        transactionId, transactionType, objectMapper.writeValueAsString(response));
                } catch (Exception e) {
                    log.warn("Failed to serialize response payload for logging - transactionId: {}", transactionId, e);
                }
            }
            
            // Process response
            if (walletModel == WalletModel.SHARED_WALLET) {
                // Shared Wallet: No local balance update, just record transaction
                if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                    transaction.setStatus(TransactionStatus.COMPLETED);
                    transaction.setCompletedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    
                    log.info("Debit successful (Shared Wallet) - transaction: {}", transactionId);
                    return buildSuccessResponse(transaction, response);
                } else {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setErrorMessage(extractErrorMessage(response));
                    transaction.setCompletedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    
                    throw new IllegalStateException("Debit operation failed");
                }
            } else {
                // Fund Transfer: Update local balance
                if (transactionType == TransactionType.TRANSFER_OUT) {
                    // TRANSFER_OUT requires operator webhook
                    if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                        wallet = walletManagementService.updateBalance(wallet, balanceAfter);
                        transaction.setBalanceAfter(balanceAfter);
                        transaction.setStatus(TransactionStatus.COMPLETED);
                        transaction.setCompletedAt(LocalDateTime.now());
                        transactionRepository.save(transaction);
                        
                        log.info("Transfer out successful - transaction: {}, balance: {}", transactionId, balanceAfter);
                        return buildSuccessResponse(transaction, response);
                    } else {
                        transaction.setStatus(TransactionStatus.FAILED);
                        transaction.setErrorMessage(extractErrorMessage(response));
                        transaction.setCompletedAt(LocalDateTime.now());
                        transactionRepository.save(transaction);
                        
                        throw new IllegalStateException("Transfer out failed");
                    }
                } else {
                    // BET, WITHDRAWAL: No operator webhook, just update local balance
                    wallet = walletManagementService.updateBalance(wallet, balanceAfter);
                    transaction.setBalanceAfter(balanceAfter);
                    transaction.setStatus(TransactionStatus.COMPLETED);
                    transaction.setCompletedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    
                    log.info("Debit successful (Fund Transfer) - transaction: {}, balance: {}", transactionId, balanceAfter);
                    return buildSuccessResponse(transaction);
                }
            }
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            log.error("Debit error - transaction: {}", transactionId, e);
            throw new RuntimeException("Debit operation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process credit transaction with balance tracking
     */
    @Transactional
    public Map<String, Object> processCredit(
            Long operatorId,
            String playerId,
            BigDecimal amount,
            String currency,
            String reference,
            String description,
            TransactionType transactionType,
            WalletModel walletModel,
            Long relatedTransactionId) {
        
        log.info("Processing credit - operator: {}, player: {}, amount: {}, type: {}, model: {}", 
            operatorId, playerId, amount, transactionType, walletModel);
        
        // Get or create wallet
        Wallet wallet = walletManagementService.getOrCreateWallet(
            operatorId, playerId, walletModel, currency);
        
        // Lock wallet
        wallet = walletManagementService.getWalletWithLock(operatorId, playerId);
        
        // Validate wallet
        walletManagementService.validateWalletStatus(wallet);
        walletManagementService.validateCurrency(wallet, currency);
        
        // Record balance before
        BigDecimal balanceBefore = wallet.getBalance();
        
        // Generate transaction ID
        String transactionId = generateTransactionId();
        
        // Check idempotency - CRITICAL: Use findByReference, not findByTransactionId
        // This prevents duplicate payouts for the same bet settlement
        if (reference != null && !reference.isEmpty()) {
            List<WalletTransaction> existing = transactionRepository.findByReference(reference);
            Optional<WalletTransaction> completed = existing.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .findFirst();
            if (completed.isPresent()) {
                log.warn("Duplicate credit transaction detected (idempotency) - reference: {}, returning existing transaction. This prevents duplicate payouts for the same bet.", reference);
                return buildSuccessResponse(completed.get());
            }
        }
        
        // Get operator wallet config
        OperatorWalletConfig config = configCacheService.getConfig(operatorId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Operator wallet config not found or disabled: " + operatorId));
        
        // Build request payload
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("playerId", playerId);
        requestPayload.put("amount", amount);
        requestPayload.put("currency", currency);
        requestPayload.put("transactionId", transactionId);
        requestPayload.put("reference", reference);
        requestPayload.put("description", description);
        
        // Log request payload (for audit trail - not stored in DB)
        try {
            log.info("Wallet transaction request - transactionId: {}, type: {}, payload: {}", 
                transactionId, transactionType, objectMapper.writeValueAsString(requestPayload));
        } catch (Exception e) {
            log.warn("Failed to serialize request payload for logging - transactionId: {}", transactionId, e);
        }
        
        // Calculate balance after
        BigDecimal balanceAfter = balanceBefore.add(amount);
        
        // Create transaction record (without payloads - they're logged instead)
        // Note: operator_url not stored - can be retrieved from operator_wallet_config table
        WalletTransaction transaction = WalletTransaction.builder()
            .wallet(wallet)
            .operatorId(operatorId)
            .playerId(playerId)
            .transactionId(transactionId)
            .transactionType(transactionType)
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(walletModel == WalletModel.FUND_TRANSFER ? balanceAfter : null)
            .currency(currency)
            .reference(reference)
            .description(description)
            .status(TransactionStatus.PENDING)
            .build();
        
        // Link to related transaction if provided
        if (relatedTransactionId != null) {
            Optional<WalletTransaction> related = transactionRepository.findById(relatedTransactionId);
            related.ifPresent(transaction::setRelatedTransaction);
        }
        
        transaction = transactionRepository.save(transaction);
        
        try {
            // Call operator webhook (for Shared Wallet model or Fund Transfer TRANSFER_IN)
            Map<String, Object> response = null;
            if (walletModel == WalletModel.SHARED_WALLET || 
                (walletModel == WalletModel.FUND_TRANSFER && transactionType == TransactionType.TRANSFER_IN)) {
                response = webhookClient.credit(config, requestPayload).block();
            }
            
            // Log response payload (for audit trail - not stored in DB)
            if (response != null) {
                try {
                    log.info("Wallet transaction response - transactionId: {}, type: {}, payload: {}", 
                        transactionId, transactionType, objectMapper.writeValueAsString(response));
                } catch (Exception e) {
                    log.warn("Failed to serialize response payload for logging - transactionId: {}", transactionId, e);
                }
            }
            
            // Process response
            if (walletModel == WalletModel.SHARED_WALLET) {
                // Shared Wallet: No local balance update
                if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                    transaction.setStatus(TransactionStatus.COMPLETED);
                    transaction.setCompletedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    
                    log.info("Credit successful (Shared Wallet) - transaction: {}", transactionId);
                    return buildSuccessResponse(transaction, response);
                } else {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setErrorMessage(extractErrorMessage(response));
                    transaction.setCompletedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    
                    throw new IllegalStateException("Credit operation failed");
                }
            } else {
                // Fund Transfer: Update local balance
                if (transactionType == TransactionType.TRANSFER_IN) {
                    // TRANSFER_IN requires operator webhook
                    if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                        wallet = walletManagementService.updateBalance(wallet, balanceAfter);
                        transaction.setBalanceAfter(balanceAfter);
                        transaction.setStatus(TransactionStatus.COMPLETED);
                        transaction.setCompletedAt(LocalDateTime.now());
                        transactionRepository.save(transaction);
                        
                        log.info("Transfer in successful - transaction: {}, balance: {}", transactionId, balanceAfter);
                        return buildSuccessResponse(transaction, response);
                    } else {
                        transaction.setStatus(TransactionStatus.FAILED);
                        transaction.setErrorMessage(extractErrorMessage(response));
                        transaction.setCompletedAt(LocalDateTime.now());
                        transactionRepository.save(transaction);
                        
                        throw new IllegalStateException("Transfer in failed");
                    }
                } else {
                    // WIN, REFUND, BONUS: No operator webhook, just update local balance
                    wallet = walletManagementService.updateBalance(wallet, balanceAfter);
                    transaction.setBalanceAfter(balanceAfter);
                    transaction.setStatus(TransactionStatus.COMPLETED);
                    transaction.setCompletedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    
                    log.info("Credit successful (Fund Transfer) - transaction: {}, balance: {}", transactionId, balanceAfter);
                    return buildSuccessResponse(transaction);
                }
            }
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            log.error("Credit error - transaction: {}", transactionId, e);
            throw new RuntimeException("Credit operation failed: " + e.getMessage(), e);
        }
    }
    
    // Helper methods
    private String generateTransactionId() {
        return "txn_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private String extractErrorMessage(Map<String, Object> response) {
        if (response == null) {
            return "No response from operator";
        }
        return (String) response.getOrDefault("message", 
               response.getOrDefault("error", "Unknown error"));
    }
    
    private Map<String, Object> buildSuccessResponse(WalletTransaction transaction) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("transactionId", transaction.getTransactionId());
        response.put("status", transaction.getStatus().name());
        if (transaction.getBalanceAfter() != null) {
            response.put("balance", transaction.getBalanceAfter());
        }
        return response;
    }
    
    private Map<String, Object> buildSuccessResponse(WalletTransaction transaction, Map<String, Object> webhookResponse) {
        Map<String, Object> response = buildSuccessResponse(transaction);
        if (webhookResponse != null) {
            response.putAll(webhookResponse);
        }
        return response;
    }
}
