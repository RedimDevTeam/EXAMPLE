package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.model.OperatorWalletConfig;
import com.b2bplatform.wallet.repository.OperatorWalletConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Service for caching operator wallet configurations in Redis
 */
@Service
@Slf4j
public class OperatorWalletConfigCacheService {
    
    private final OperatorWalletConfigRepository configRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${wallet.config-cache-ttl-hours:24}")
    private int configCacheTtlHours;
    
    private static final String CACHE_KEY_PREFIX = "wallet:config:operator:";
    
    public OperatorWalletConfigCacheService(
            OperatorWalletConfigRepository configRepository,
            @Qualifier("redisTemplateObject") RedisTemplate<String, Object> redisTemplate) {
        this.configRepository = configRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.objectMapper.findAndRegisterModules();
    }
    
    /**
     * Get operator wallet config from cache (Redis) or database
     * Checks Redis first, falls back to database if not found
     */
    public Optional<OperatorWalletConfig> getConfig(Long operatorId) {
        String cacheKey = CACHE_KEY_PREFIX + operatorId;
        
        // Try Redis first
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for operator wallet config: {}", operatorId);
                if (cached instanceof OperatorWalletConfig) {
                    OperatorWalletConfig config = (OperatorWalletConfig) cached;
                    // Only return if enabled
                    if (Boolean.TRUE.equals(config.getEnabled())) {
                        return Optional.of(config);
                    } else {
                        log.debug("Cached config is disabled, removing from cache");
                        invalidateCache(operatorId);
                        return Optional.empty();
                    }
                } else if (cached instanceof java.util.Map) {
                    // Deserialize from Map
                    OperatorWalletConfig config = objectMapper.convertValue(cached, OperatorWalletConfig.class);
                    if (Boolean.TRUE.equals(config.getEnabled())) {
                        return Optional.of(config);
                    } else {
                        invalidateCache(operatorId);
                        return Optional.empty();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error reading from Redis cache: {}", e.getMessage());
        }
        
        // Cache miss - fetch from database
        log.debug("Cache miss for operator wallet config: {}, fetching from database", operatorId);
        Optional<OperatorWalletConfig> config = configRepository.findByOperatorIdAndEnabledTrue(operatorId);
        
        // Store in cache if found
        if (config.isPresent()) {
            cacheConfig(config.get());
        }
        
        return config;
    }
    
    /**
     * Cache operator wallet config in Redis
     */
    public void cacheConfig(OperatorWalletConfig config) {
        String cacheKey = CACHE_KEY_PREFIX + config.getOperatorId();
        
        try {
            redisTemplate.opsForValue().set(
                cacheKey,
                config,
                Duration.ofHours(configCacheTtlHours)
            );
            log.debug("Cached operator wallet config: {}", config.getOperatorId());
        } catch (Exception e) {
            log.warn("Error caching operator wallet config: {}", e.getMessage());
        }
    }
    
    /**
     * Invalidate cache for an operator
     */
    public void invalidateCache(Long operatorId) {
        String cacheKey = CACHE_KEY_PREFIX + operatorId;
        
        try {
            redisTemplate.delete(cacheKey);
            log.debug("Invalidated cache for operator wallet config: {}", operatorId);
        } catch (Exception e) {
            log.warn("Error invalidating cache: {}", e.getMessage());
        }
    }
    
    /**
     * Refresh cache from database
     */
    public void refreshCache(Long operatorId) {
        invalidateCache(operatorId);
        Optional<OperatorWalletConfig> config = configRepository.findByOperatorId(operatorId);
        if (config.isPresent()) {
            cacheConfig(config.get());
        }
    }
}
