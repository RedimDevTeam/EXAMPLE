package com.b2bplatform.b2c.service;

import com.b2bplatform.b2c.dto.request.*;
import com.b2bplatform.b2c.dto.response.ProviderBalanceResponse;
import com.b2bplatform.b2c.dto.response.ProviderWalletResponse;
import com.b2bplatform.b2c.model.ProviderConfig;
import com.b2bplatform.b2c.model.ProviderTransaction;
import com.b2bplatform.b2c.model.ProviderTransaction.TransactionStatus;
import com.b2bplatform.b2c.model.ProviderTransaction.UnitType;
import com.b2bplatform.b2c.repository.ProviderTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for B2C provider wallet operations
 * Handles debit, credit, refund, cancel, and balance queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class B2CProviderWalletService {
    
    private final ProviderConfigService providerConfigService;
    private final B2CProviderClient providerClient;
    private final ProviderTransactionRepository transactionRepository;
    
    /**
     * Debit player wallet via provider API
     */
    @Transactional
    public ProviderWalletResponse debit(String providerId, ProviderDebitRequest request) {
        log.info("Debiting player wallet: providerId={}, playerId={}, amount={}", 
            providerId, request.getPlayerId(), request.getAmount());
        
        // Get provider configuration
        ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
        if (!providerConfig.getIsActive()) {
            throw new IllegalStateException("Provider is not active: " + providerId);
        }
        
        // Check for duplicate transaction (idempotency)
        ProviderTransaction existingTransaction = transactionRepository
            .findByProviderIdAndTransactionId(providerId, request.getTransactionId())
            .orElse(null);
        
        if (existingTransaction != null) {
            log.warn("Duplicate transaction detected: providerId={}, transactionId={}", 
                providerId, request.getTransactionId());
            
            // Return existing transaction result
            return buildResponseFromTransaction(existingTransaction);
        }
        
        // Create transaction record
        ProviderTransaction transaction = ProviderTransaction.builder()
                .providerId(providerId)
                .playerId(request.getPlayerId())
                .transactionId(request.getTransactionId())
                .transactionSubtypeId(request.getTransactionSubtypeId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .unitType(UnitType.valueOf(request.getUnitType()))
                .playerLevel(request.getPlayerLevel())
                .gameId(request.getGameId())
                .roundId(request.getRoundId())
                .handId(request.getHandId())
                .brandId(request.getBrandId())
                .agentId(request.getAgentId())
                .language(request.getLanguage())
                .status(TransactionStatus.PENDING)
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        try {
            // Build request payload for provider
            Map<String, Object> providerRequest = buildDebitRequest(request);
            
            // Call provider API
            Map<String, Object> providerResponse = providerClient
                .callProviderApi(providerConfig, "/wallet/debit", providerRequest, "DEBIT")
                .block(); // Block for synchronous operation
            
            // Process response
            ProviderWalletResponse response = processProviderResponse(providerResponse, transaction);
            
            // Update transaction
            transaction.setProviderResponse(providerResponse);
            if (response.getStatus() == 1000) { // Success
                transaction.markCompleted();
            } else {
                transaction.markFailed(response.getMessage());
            }
            transactionRepository.save(transaction);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error calling provider debit API: {}", e.getMessage(), e);
            transaction.markFailed(e.getMessage());
            transactionRepository.save(transaction);
            
            return ProviderWalletResponse.builder()
                    .status(5000) // System Error
                    .message("Provider API error: " + e.getMessage())
                    .transactionId(request.getTransactionId())
                    .build();
        }
    }
    
    /**
     * Credit player wallet via provider API
     */
    @Transactional
    public ProviderWalletResponse credit(String providerId, ProviderCreditRequest request) {
        log.info("Crediting player wallet: providerId={}, playerId={}, amount={}", 
            providerId, request.getPlayerId(), request.getAmount());
        
        ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
        if (!providerConfig.getIsActive()) {
            throw new IllegalStateException("Provider is not active: " + providerId);
        }
        
        // Check for duplicate transaction
        ProviderTransaction existingTransaction = transactionRepository
            .findByProviderIdAndTransactionId(providerId, request.getTransactionId())
            .orElse(null);
        
        if (existingTransaction != null) {
            log.warn("Duplicate transaction detected: providerId={}, transactionId={}", 
                providerId, request.getTransactionId());
            return buildResponseFromTransaction(existingTransaction);
        }
        
        // Create transaction record
        ProviderTransaction transaction = ProviderTransaction.builder()
                .providerId(providerId)
                .playerId(request.getPlayerId())
                .transactionId(request.getTransactionId())
                .transactionSubtypeId(request.getTransactionSubtypeId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .unitType(UnitType.valueOf(request.getUnitType()))
                .playerLevel(request.getPlayerLevel())
                .gameId(request.getGameId())
                .roundId(request.getRoundId())
                .handId(request.getHandId())
                .brandId(request.getBrandId())
                .agentId(request.getAgentId())
                .language(request.getLanguage())
                .status(TransactionStatus.PENDING)
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        try {
            Map<String, Object> providerRequest = buildCreditRequest(request);
            Map<String, Object> providerResponse = providerClient
                .callProviderApi(providerConfig, "/wallet/credit", providerRequest, "CREDIT")
                .block();
            
            ProviderWalletResponse response = processProviderResponse(providerResponse, transaction);
            
            transaction.setProviderResponse(providerResponse);
            if (response.getStatus() == 1000) {
                transaction.markCompleted();
            } else {
                transaction.markFailed(response.getMessage());
            }
            transactionRepository.save(transaction);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error calling provider credit API: {}", e.getMessage(), e);
            transaction.markFailed(e.getMessage());
            transactionRepository.save(transaction);
            
            return ProviderWalletResponse.builder()
                    .status(5000)
                    .message("Provider API error: " + e.getMessage())
                    .transactionId(request.getTransactionId())
                    .build();
        }
    }
    
    /**
     * Refund transaction via provider API
     */
    @Transactional
    public ProviderWalletResponse refund(String providerId, ProviderRefundRequest request) {
        log.info("Refunding transaction: providerId={}, originalTransactionId={}", 
            providerId, request.getOriginalTransactionId());
        
        ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
        if (!providerConfig.getIsActive()) {
            throw new IllegalStateException("Provider is not active: " + providerId);
        }
        
        // Check for duplicate refund transaction
        ProviderTransaction existingTransaction = transactionRepository
            .findByProviderIdAndTransactionId(providerId, request.getTransactionId())
            .orElse(null);
        
        if (existingTransaction != null) {
            log.warn("Duplicate refund transaction detected: providerId={}, transactionId={}", 
                providerId, request.getTransactionId());
            return buildResponseFromTransaction(existingTransaction);
        }
        
        // Create refund transaction record
        ProviderTransaction transaction = ProviderTransaction.builder()
                .providerId(providerId)
                .playerId(request.getPlayerId())
                .transactionId(request.getTransactionId())
                .transactionSubtypeId(request.getTransactionSubtypeId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .unitType(UnitType.valueOf(request.getUnitType()))
                .roundId(request.getRoundId())
                .gameId(request.getGameId())
                .status(TransactionStatus.PENDING)
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        try {
            Map<String, Object> providerRequest = buildRefundRequest(request);
            Map<String, Object> providerResponse = providerClient
                .callProviderApi(providerConfig, "/wallet/refund", providerRequest, "REFUND")
                .block();
            
            ProviderWalletResponse response = processProviderResponse(providerResponse, transaction);
            
            transaction.setProviderResponse(providerResponse);
            if (response.getStatus() == 1000) {
                transaction.markCompleted();
            } else {
                transaction.markFailed(response.getMessage());
            }
            transactionRepository.save(transaction);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error calling provider refund API: {}", e.getMessage(), e);
            transaction.markFailed(e.getMessage());
            transactionRepository.save(transaction);
            
            return ProviderWalletResponse.builder()
                    .status(5000)
                    .message("Provider API error: " + e.getMessage())
                    .transactionId(request.getTransactionId())
                    .build();
        }
    }
    
    /**
     * Cancel transaction via provider API
     */
    @Transactional
    public ProviderWalletResponse cancel(String providerId, String transactionId, String playerId) {
        log.info("Cancelling transaction: providerId={}, transactionId={}", providerId, transactionId);
        
        ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
        if (!providerConfig.getIsActive()) {
            throw new IllegalStateException("Provider is not active: " + providerId);
        }
        
        try {
            Map<String, Object> providerRequest = new HashMap<>();
            providerRequest.put("transactionId", transactionId);
            providerRequest.put("playerId", playerId);
            
            Map<String, Object> providerResponse = providerClient
                .callProviderApi(providerConfig, "/wallet/cancel", providerRequest, "CANCEL")
                .block();
            
            // Process response
            Integer status = extractStatus(providerResponse);
            String message = extractMessage(providerResponse);
            BigDecimal balance = extractBalance(providerResponse);
            String currency = extractCurrency(providerResponse);
            String unitType = extractUnitType(providerResponse);
            
            return ProviderWalletResponse.builder()
                    .status(status != null ? status : 1000)
                    .balance(balance)
                    .currency(currency)
                    .unitType(unitType)
                    .message(message)
                    .transactionId(transactionId)
                    .build();
            
        } catch (Exception e) {
            log.error("Error calling provider cancel API: {}", e.getMessage(), e);
            return ProviderWalletResponse.builder()
                    .status(5000)
                    .message("Provider API error: " + e.getMessage())
                    .transactionId(transactionId)
                    .build();
        }
    }
    
    /**
     * Get player balance via provider API
     */
    @Transactional(readOnly = true)
    public ProviderBalanceResponse getBalance(String providerId, ProviderBalanceRequest request) {
        log.info("Getting player balance: providerId={}, playerId={}", providerId, request.getPlayerId());
        
        ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
        if (!providerConfig.getIsActive()) {
            throw new IllegalStateException("Provider is not active: " + providerId);
        }
        
        try {
            String endpoint = "/wallet/balance?playerId=" + request.getPlayerId();
            if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
                endpoint += "&currency=" + request.getCurrency();
            }
            
            Map<String, Object> providerResponse = providerClient
                .callProviderApiGet(providerConfig, endpoint, "BALANCE")
                .block();
            
            Integer status = extractStatus(providerResponse);
            BigDecimal balance = extractBalance(providerResponse);
            String currency = extractCurrency(providerResponse);
            String unitType = extractUnitType(providerResponse);
            String message = extractMessage(providerResponse);
            
            return ProviderBalanceResponse.builder()
                    .status(status != null ? status : 1000)
                    .playerId(request.getPlayerId())
                    .balance(balance)
                    .currency(currency)
                    .unitType(unitType)
                    .message(message)
                    .build();
            
        } catch (Exception e) {
            log.error("Error calling provider balance API: {}", e.getMessage(), e);
            return ProviderBalanceResponse.builder()
                    .status(5000)
                    .playerId(request.getPlayerId())
                    .message("Provider API error: " + e.getMessage())
                    .build();
        }
    }
    
    // Helper methods
    
    private Map<String, Object> buildDebitRequest(ProviderDebitRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("playerId", request.getPlayerId());
        payload.put("amount", request.getAmount());
        payload.put("currency", request.getCurrency());
        payload.put("unitType", request.getUnitType());
        payload.put("transactionId", request.getTransactionId());
        payload.put("transactionSubtypeId", request.getTransactionSubtypeId());
        payload.put("playerLevel", request.getPlayerLevel());
        payload.put("gameId", request.getGameId());
        payload.put("roundId", request.getRoundId());
        if (request.getHandId() != null) payload.put("handId", request.getHandId());
        if (request.getBrandId() != null) payload.put("brandId", request.getBrandId());
        if (request.getAgentId() != null) payload.put("agentId", request.getAgentId());
        if (request.getLanguage() != null) payload.put("language", request.getLanguage());
        return payload;
    }
    
    private Map<String, Object> buildCreditRequest(ProviderCreditRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("playerId", request.getPlayerId());
        payload.put("amount", request.getAmount());
        payload.put("currency", request.getCurrency());
        payload.put("unitType", request.getUnitType());
        payload.put("transactionId", request.getTransactionId());
        payload.put("transactionSubtypeId", request.getTransactionSubtypeId());
        payload.put("playerLevel", request.getPlayerLevel());
        payload.put("gameId", request.getGameId());
        payload.put("roundId", request.getRoundId());
        if (request.getHandId() != null) payload.put("handId", request.getHandId());
        if (request.getBrandId() != null) payload.put("brandId", request.getBrandId());
        if (request.getAgentId() != null) payload.put("agentId", request.getAgentId());
        if (request.getLanguage() != null) payload.put("language", request.getLanguage());
        return payload;
    }
    
    private Map<String, Object> buildRefundRequest(ProviderRefundRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("playerId", request.getPlayerId());
        payload.put("originalTransactionId", request.getOriginalTransactionId());
        payload.put("transactionId", request.getTransactionId());
        payload.put("amount", request.getAmount());
        payload.put("currency", request.getCurrency());
        payload.put("unitType", request.getUnitType());
        payload.put("transactionSubtypeId", request.getTransactionSubtypeId());
        payload.put("roundId", request.getRoundId());
        payload.put("gameId", request.getGameId());
        return payload;
    }
    
    private ProviderWalletResponse processProviderResponse(Map<String, Object> providerResponse, ProviderTransaction transaction) {
        Integer status = extractStatus(providerResponse);
        BigDecimal balance = extractBalance(providerResponse);
        String currency = extractCurrency(providerResponse);
        String unitType = extractUnitType(providerResponse);
        String message = extractMessage(providerResponse);
        
        return ProviderWalletResponse.builder()
                .status(status != null ? status : 1000)
                .balance(balance)
                .currency(currency != null ? currency : transaction.getCurrency())
                .unitType(unitType != null ? unitType : transaction.getUnitType().name())
                .message(message)
                .transactionId(transaction.getTransactionId())
                .build();
    }
    
    private ProviderWalletResponse buildResponseFromTransaction(ProviderTransaction transaction) {
        Object providerResponseObj = transaction.getProviderResponse();
        if (providerResponseObj != null && providerResponseObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> providerResponse = (Map<String, Object>) providerResponseObj;
            return processProviderResponse(providerResponse, transaction);
        }
        
        // If no provider response, return basic response
        return ProviderWalletResponse.builder()
                .status(transaction.getStatus() == TransactionStatus.COMPLETED ? 1000 : 5000)
                .currency(transaction.getCurrency())
                .unitType(transaction.getUnitType().name())
                .message(transaction.getErrorMessage() != null ? transaction.getErrorMessage() : "Transaction already processed")
                .transactionId(transaction.getTransactionId())
                .build();
    }
    
    private Integer extractStatus(Map<String, Object> response) {
        if (response == null) return null;
        Object status = response.get("status");
        if (status instanceof Integer) return (Integer) status;
        if (status instanceof Number) return ((Number) status).intValue();
        return null;
    }
    
    private BigDecimal extractBalance(Map<String, Object> response) {
        if (response == null) return null;
        Object balance = response.get("balance");
        if (balance instanceof BigDecimal) return (BigDecimal) balance;
        if (balance instanceof Number) return BigDecimal.valueOf(((Number) balance).doubleValue());
        return null;
    }
    
    private String extractCurrency(Map<String, Object> response) {
        if (response == null) return null;
        Object currency = response.get("currency");
        return currency != null ? currency.toString() : null;
    }
    
    private String extractUnitType(Map<String, Object> response) {
        if (response == null) return null;
        Object unitType = response.get("unitType");
        return unitType != null ? unitType.toString() : null;
    }
    
    private String extractMessage(Map<String, Object> response) {
        if (response == null) return null;
        Object message = response.get("message");
        return message != null ? message.toString() : null;
    }
}
