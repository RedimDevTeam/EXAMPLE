package com.b2bplatform.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for API key validation results (replaces Redis for gateway).
 * Entries expire after TTL.
 */
@Component
@Slf4j
public class InMemoryApiKeyCache {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public Optional<Long> get(String apiKey) {
        CacheEntry entry = cache.get(apiKey);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            cache.remove(apiKey);
            return Optional.empty();
        }
        return Optional.of(entry.operatorId);
    }

    public void put(String apiKey, Long operatorId) {
        cache.put(apiKey, new CacheEntry(operatorId, System.currentTimeMillis() + CACHE_TTL.toMillis()));
    }

    private static final class CacheEntry {
        final long operatorId;
        final long expiryAt;

        CacheEntry(long operatorId, long expiryAt) {
            this.operatorId = operatorId;
            this.expiryAt = expiryAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryAt;
        }
    }
}
