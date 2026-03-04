package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.dto.response.BalanceResponse;
import com.b2bplatform.wallet.dto.response.CreditResponse;
import com.b2bplatform.wallet.dto.response.DebitResponse;
import com.b2bplatform.wallet.model.OperatorWalletConfig;
import com.b2bplatform.wallet.model.TransactionType;
import com.b2bplatform.wallet.model.Wallet;
import com.b2bplatform.wallet.model.Wallet.WalletModel;
import com.b2bplatform.wallet.model.WalletTransaction;
import com.b2bplatform.wallet.model.WalletTransaction.TransactionStatus;
import com.b2bplatform.wallet.repository.OperatorWalletConfigRepository;
import com.b2bplatform.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    private final OperatorWalletConfigRepository configRepository;
    private final WalletTransactionRepository transactionRepository;
    private final OperatorServiceClient operatorServiceClient;
    private final OperatorWebhookClient webhookClient;
    private final OperatorWalletConfigCacheService configCacheService;
    private final WalletManagementService walletManagementService;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${wallet.idempotency-ttl-hours:24}")
    private int idempotencyTtlHours;
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "wallet:txn:";
    
    // ObjectMapper for JSON logging (static final - thread-safe, shared across instances)
    private static final ObjectMapper objectMapper;
    
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        objectMapper = mapper;
    }
    
    /**
     * Debit player wallet (for bet placement)
     */
    @Transactional
    public DebitResponse debit(Long operatorId, String playerId, BigDecimal amount, 
                                String currency, String reference, String description) {
        log.info("Debit request - operator: {}, player: {}, amount: {}", operatorId, playerId, amount);
        
        // Generate transaction ID
        String transactionId = generateTransactionId();
        
        // Check idempotency (if reference provided) - CRITICAL: Use findByReference, not findByTransactionId
        if (reference != null && !reference.isEmpty()) {
            List<WalletTransaction> existing = transactionRepository.findByReference(reference);
            Optional<WalletTransaction> completed = existing.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .findFirst();
            if (completed.isPresent()) {
                log.warn("Duplicate transaction detected (idempotency) - reference: {}, returning existing transaction", reference);
                return buildDebitResponse(completed.get(), null);
            }
        }
        
        // Get operator wallet config (from Redis cache or database)
        OperatorWalletConfig config = configCacheService.getConfig(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("Operator wallet config not found or disabled: " + operatorId));
        
        // Build request
        Map<String, Object> request = new HashMap<>();
        request.put("playerId", playerId);
        request.put("amount", amount);
        request.put("currency", currency != null ? currency : "USD");
        request.put("transactionId", transactionId);
        request.put("reference", reference);
        request.put("description", description != null ? description : "Wallet debit");
        
        // Log request payload (for audit trail - not stored in DB)
        try {
            log.info("Wallet transaction request - transactionId: {}, type: BET, payload: {}", 
                transactionId, objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.warn("Failed to serialize request payload for logging - transactionId: {}", transactionId, e);
        }
        
        // Get or create wallet (Shared Wallet model by default for backward compatibility)
        Wallet wallet = walletManagementService.getOrCreateWallet(
            operatorId, playerId, WalletModel.SHARED_WALLET, currency != null ? currency : "USD");
        
        // Create transaction record (without payloads - they're logged instead)
        // Note: operator_url not stored - can be retrieved from operator_wallet_config table
        WalletTransaction transaction = WalletTransaction.builder()
            .wallet(wallet)
            .operatorId(operatorId)
            .playerId(playerId)
            .transactionId(transactionId)
            .transactionType(TransactionType.BET) // BET is a debit transaction
            .amount(amount)
            .currency(currency != null ? currency : "USD")
            .status(TransactionStatus.PENDING)
            .reference(reference)
            .description(description != null ? description : "Wallet debit")
            .build();
        transaction = transactionRepository.save(transaction);
        
        try {
            // Call operator webhook
            Map<String, Object> response = webhookClient.debit(config, request).block();
            
            // Log response payload (for audit trail - not stored in DB)
            if (response != null) {
                try {
                    log.info("Wallet transaction response - transactionId: {}, type: BET, payload: {}", 
                        transactionId, objectMapper.writeValueAsString(response));
                } catch (Exception e) {
                    log.warn("Failed to serialize response payload for logging - transactionId: {}", transactionId, e);
                }
            }
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                // Success
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                
                // Store idempotency key
                storeIdempotencyKey(transactionId, transaction);
                
                BigDecimal balance = response.get("balance") != null ? 
                    new BigDecimal(response.get("balance").toString()) : null;
                
                log.info("Debit successful - transaction: {}, balance: {}", transactionId, balance);
                return buildDebitResponse(transaction, response);
            } else {
                // Failed
                String errorMsg = extractErrorMessage(response);
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorMessage(errorMsg);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                
                log.warn("Debit failed - transaction: {}, error: {}", transactionId, errorMsg);
                throw new IllegalStateException(errorMsg != null ? errorMsg : "Debit operation failed");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Re-throw business exceptions
            throw e;
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
     * Credit player wallet (for win payout)
     */
    @Transactional
    public CreditResponse credit(Long operatorId, String playerId, BigDecimal amount, 
                                 String currency, String reference, String description) {
        log.info("Credit request - operator: {}, player: {}, amount: {}", operatorId, playerId, amount);
        
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
                return buildCreditResponse(completed.get(), null);
            }
        }
        
        // Get operator wallet config (from Redis cache or database)
        OperatorWalletConfig config = configCacheService.getConfig(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("Operator wallet config not found or disabled: " + operatorId));
        
        // Build request
        Map<String, Object> request = new HashMap<>();
        request.put("playerId", playerId);
        request.put("amount", amount);
        request.put("currency", currency != null ? currency : "USD");
        request.put("transactionId", transactionId);
        request.put("reference", reference);
        request.put("description", description != null ? description : "Wallet credit");
        
        // Log request payload (for audit trail - not stored in DB)
        try {
            log.info("Wallet transaction request - transactionId: {}, type: WIN, payload: {}", 
                transactionId, objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.warn("Failed to serialize request payload for logging - transactionId: {}", transactionId, e);
        }
        
        // Get or create wallet (Shared Wallet model by default for backward compatibility)
        Wallet wallet = walletManagementService.getOrCreateWallet(
            operatorId, playerId, WalletModel.SHARED_WALLET, currency != null ? currency : "USD");
        
        // Create transaction record (without payloads - they're logged instead)
        // Note: operator_url not stored - can be retrieved from operator_wallet_config table
        WalletTransaction transaction = WalletTransaction.builder()
            .wallet(wallet)
            .operatorId(operatorId)
            .playerId(playerId)
            .transactionId(transactionId)
            .transactionType(TransactionType.WIN) // WIN is a credit transaction
            .amount(amount)
            .currency(currency != null ? currency : "USD")
            .status(TransactionStatus.PENDING)
            .reference(reference)
            .description(description != null ? description : "Wallet credit")
            .build();
        transaction = transactionRepository.save(transaction);
        
        try {
            // Call operator webhook
            Map<String, Object> response = webhookClient.credit(config, request).block();
            
            // Log response payload (for audit trail - not stored in DB)
            if (response != null) {
                try {
                    log.info("Wallet transaction response - transactionId: {}, type: WIN, payload: {}", 
                        transactionId, objectMapper.writeValueAsString(response));
                } catch (Exception e) {
                    log.warn("Failed to serialize response payload for logging - transactionId: {}", transactionId, e);
                }
            }
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                // Success
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                
                // Store idempotency key
                storeIdempotencyKey(transactionId, transaction);
                
                BigDecimal balance = response.get("balance") != null ? 
                    new BigDecimal(response.get("balance").toString()) : null;
                
                log.info("Credit successful - transaction: {}, balance: {}", transactionId, balance);
                return buildCreditResponse(transaction, response);
            } else {
                // Failed
                String errorMsg = extractErrorMessage(response);
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorMessage(errorMsg);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                
                log.warn("Credit failed - transaction: {}, error: {}", transactionId, errorMsg);
                throw new IllegalStateException(errorMsg != null ? errorMsg : "Credit operation failed");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Re-throw business exceptions
            throw e;
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            log.error("Credit error - transaction: {}", transactionId, e);
            throw new RuntimeException("Credit operation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Query player balance
     */
    @Transactional
    public BalanceResponse getBalance(Long operatorId, String playerId) {
        log.info("Balance query - operator: {}, player: {}", operatorId, playerId);
        
        // Get operator wallet config (from Redis cache or database)
        OperatorWalletConfig config = configCacheService.getConfig(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("Operator wallet config not found or disabled: " + operatorId));
        
        // Generate transaction ID for tracking
        String transactionId = generateTransactionId();
        
        // Get or create wallet (Shared Wallet model by default for backward compatibility)
        Wallet wallet = walletManagementService.getOrCreateWallet(
            operatorId, playerId, WalletModel.SHARED_WALLET, "USD");
        
        // Create transaction record (without payloads - they're logged instead)
        // Note: operator_url not stored - can be retrieved from operator_wallet_config table
        WalletTransaction transaction = WalletTransaction.builder()
            .wallet(wallet)
            .operatorId(operatorId)
            .playerId(playerId)
            .transactionId(transactionId)
            .transactionType(TransactionType.BALANCE_QUERY)
            .amount(BigDecimal.ZERO) // Balance query has no amount
            .currency("USD")
            .status(TransactionStatus.PENDING)
            .build();
        transaction = transactionRepository.save(transaction);
        
        try {
            // Call operator webhook
            Map<String, Object> response = webhookClient.balance(config, playerId).block();
            
            // Log response payload (for audit trail - not stored in DB)
            if (response != null) {
                try {
                    log.info("Wallet transaction response - transactionId: {}, type: BALANCE_QUERY, payload: {}", 
                        transactionId, objectMapper.writeValueAsString(response));
                } catch (Exception e) {
                    log.warn("Failed to serialize response payload for logging - transactionId: {}", transactionId, e);
                }
            }
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                // Success
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                
                BigDecimal balance = response.get("balance") != null ? 
                    new BigDecimal(response.get("balance").toString()) : null;
                
                log.info("Balance query successful - player: {}, balance: {}", playerId, balance);
                return buildBalanceResponse(playerId, response, transaction);
            } else {
                // Failed
                String errorMsg = extractErrorMessage(response);
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorMessage(errorMsg);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                
                log.warn("Balance query failed - player: {}, error: {}", playerId, errorMsg);
                throw new IllegalStateException(errorMsg != null ? errorMsg : "Balance query failed");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Re-throw business exceptions
            throw e;
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            log.error("Balance query error - player: {}", playerId, e);
            throw new RuntimeException("Balance query failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get transaction history for a player
     */
    public List<WalletTransaction> getTransactionHistory(String playerId, Integer limit) {
        List<WalletTransaction> transactions = transactionRepository.findByPlayerIdOrderByCreatedAtDesc(playerId);
        
        if (limit != null && limit > 0) {
            return transactions.stream().limit(limit).toList();
        }
        
        return transactions;
    }
    
    /**
     * Get transaction by ID
     */
    public Optional<WalletTransaction> getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }
    
    // Helper methods
    private String generateTransactionId() {
        return "txn_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private void storeIdempotencyKey(String transactionId, WalletTransaction transaction) {
        try {
            String key = IDEMPOTENCY_KEY_PREFIX + transactionId;
            redisTemplate.opsForValue().set(key, transaction.getStatus().name(), 
                Duration.ofHours(idempotencyTtlHours));
        } catch (Exception e) {
            log.warn("Error storing idempotency key: {}", e.getMessage());
        }
    }
    
    /**
     * Build DebitResponse from transaction and operator response
     */
    private DebitResponse buildDebitResponse(WalletTransaction transaction, Map<String, Object> operatorResponse) {
        DebitResponse.DebitResponseBuilder builder = DebitResponse.builder()
            .success(true)
            .transactionId(transaction.getTransactionId())
            .status(transaction.getStatus().name())
            .timestamp(transaction.getCompletedAt() != null ? transaction.getCompletedAt() : LocalDateTime.now())
            .currency(transaction.getCurrency());
        
        if (operatorResponse != null) {
            Object balance = operatorResponse.get("balance");
            if (balance != null) {
                builder.balance(new BigDecimal(balance.toString()));
            }
        }
        
        return builder.build();
    }
    
    /**
     * Build CreditResponse from transaction and operator response
     */
    private CreditResponse buildCreditResponse(WalletTransaction transaction, Map<String, Object> operatorResponse) {
        CreditResponse.CreditResponseBuilder builder = CreditResponse.builder()
            .success(true)
            .transactionId(transaction.getTransactionId())
            .status(transaction.getStatus().name())
            .timestamp(transaction.getCompletedAt() != null ? transaction.getCompletedAt() : LocalDateTime.now())
            .currency(transaction.getCurrency());
        
        if (operatorResponse != null) {
            Object balance = operatorResponse.get("balance");
            if (balance != null) {
                builder.balance(new BigDecimal(balance.toString()));
            }
        }
        
        return builder.build();
    }
    
    /**
     * Build BalanceResponse from operator response
     */
    private BalanceResponse buildBalanceResponse(String playerId, Map<String, Object> operatorResponse, WalletTransaction transaction) {
        BalanceResponse.BalanceResponseBuilder builder = BalanceResponse.builder()
            .success(true)
            .playerId(playerId)
            .timestamp(LocalDateTime.now())
            .currency(transaction.getCurrency());
        
        if (operatorResponse != null) {
            Object balance = operatorResponse.get("balance");
            if (balance != null) {
                builder.balance(new BigDecimal(balance.toString()));
            }
            
            Object availableBalance = operatorResponse.get("availableBalance");
            if (availableBalance != null) {
                builder.availableBalance(new BigDecimal(availableBalance.toString()));
            }
            
            Object lockedBalance = operatorResponse.get("lockedBalance");
            if (lockedBalance != null) {
                builder.lockedBalance(new BigDecimal(lockedBalance.toString()));
            }
        }
        
        return builder.build();
    }
    
    private String extractErrorMessage(Map<String, Object> response) {
        if (response == null) {
            return "No response from operator";
        }
        
        Object errorObj = response.get("error");
        if (errorObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> error = (Map<String, Object>) errorObj;
            Object message = error.get("message");
            return message != null ? message.toString() : "Unknown error";
        }
        
        return "Operation failed";
    }
}
