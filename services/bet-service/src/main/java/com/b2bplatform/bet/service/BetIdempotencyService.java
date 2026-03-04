package com.b2bplatform.bet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle idempotency for bet placement and settlement.
 * Prevents duplicate bet posts and duplicate settlements due to network issues.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BetIdempotencyService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${bet.idempotency-ttl-hours:24}")
    private int idempotencyTtlHours;
    
    private static final String BET_IDEMPOTENCY_PREFIX = "bet:idempotency:";
    private static final String SETTLEMENT_IDEMPOTENCY_PREFIX = "bet:settlement:idempotency:";
    
    /**
     * Check if bet was already processed (idempotency check).
     * 
     * @param betId Unique bet ID
     * @return true if bet was already processed, false otherwise
     */
    public boolean isBetAlreadyProcessed(String betId) {
        String key = BET_IDEMPOTENCY_PREFIX + betId;
        Boolean exists = redisTemplate.hasKey(key);
        boolean processed = Boolean.TRUE.equals(exists);
        
        if (processed) {
            log.warn("Duplicate bet detected - betId: {} already processed", betId);
        }
        
        return processed;
    }
    
    /**
     * Mark bet as processed (store idempotency key).
     * 
     * @param betId Unique bet ID
     * @param betResponse Bet response to store (for duplicate detection)
     */
    public void markBetAsProcessed(String betId, String betResponseJson) {
        String key = BET_IDEMPOTENCY_PREFIX + betId;
        redisTemplate.opsForValue().set(
            key, 
            betResponseJson, 
            Duration.ofHours(idempotencyTtlHours)
        );
        log.debug("Marked bet as processed - betId: {}", betId);
    }
    
    /**
     * Get previously processed bet response (for duplicate requests).
     * 
     * @param betId Unique bet ID
     * @return Previously processed bet response JSON, or null if not found
     */
    public String getProcessedBetResponse(String betId) {
        String key = BET_IDEMPOTENCY_PREFIX + betId;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * Check if settlement was already processed (idempotency check).
     * 
     * @param betId Bet ID
     * @param settlementReference Settlement reference (e.g., "SETTLE_BET_12345")
     * @return true if settlement was already processed, false otherwise
     */
    public boolean isSettlementAlreadyProcessed(String betId, String settlementReference) {
        String key = SETTLEMENT_IDEMPOTENCY_PREFIX + betId + ":" + settlementReference;
        Boolean exists = redisTemplate.hasKey(key);
        boolean processed = Boolean.TRUE.equals(exists);
        
        if (processed) {
            log.warn("Duplicate settlement detected - betId: {}, reference: {} already processed", 
                betId, settlementReference);
        }
        
        return processed;
    }
    
    /**
     * Mark settlement as processed (store idempotency key).
     * 
     * @param betId Bet ID
     * @param settlementReference Settlement reference
     * @param settlementResponse Settlement response to store
     */
    public void markSettlementAsProcessed(String betId, String settlementReference, String settlementResponseJson) {
        String key = SETTLEMENT_IDEMPOTENCY_PREFIX + betId + ":" + settlementReference;
        redisTemplate.opsForValue().set(
            key, 
            settlementResponseJson, 
            Duration.ofHours(idempotencyTtlHours)
        );
        log.debug("Marked settlement as processed - betId: {}, reference: {}", betId, settlementReference);
    }
    
    /**
     * Get previously processed settlement response (for duplicate requests).
     * 
     * @param betId Bet ID
     * @param settlementReference Settlement reference
     * @return Previously processed settlement response JSON, or null if not found
     */
    public String getProcessedSettlementResponse(String betId, String settlementReference) {
        String key = SETTLEMENT_IDEMPOTENCY_PREFIX + betId + ":" + settlementReference;
        return redisTemplate.opsForValue().get(key);
    }
}
