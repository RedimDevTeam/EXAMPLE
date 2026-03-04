package com.b2bplatform.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Service for API key validation logic.
 * Uses in-memory cache (no Redis); validates with Operator Service via Eureka.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiKeyValidationService {

    private final OperatorServiceClient operatorServiceClient;
    private final InMemoryApiKeyCache apiKeyCache;

    /**
     * Validate API key with caching.
     * Checks in-memory cache first, then Operator Service if not cached.
     *
     * @param apiKey The API key to validate
     * @return Mono containing Optional&lt;Long&gt; operatorId if valid, empty if invalid
     */
    public Mono<Optional<Long>> validateApiKey(String apiKey) {
        log.debug("Validating API key: {}", maskApiKey(apiKey));

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Empty API key provided");
            return Mono.just(Optional.empty());
        }

        if (!apiKey.startsWith("b2b_")) {
            log.warn("Invalid API key format: {}", maskApiKey(apiKey));
            return Mono.just(Optional.empty());
        }

        // Check in-memory cache first
        Optional<Long> cached = apiKeyCache.get(apiKey);
        if (cached.isPresent()) {
            log.debug("API key found in cache for operator: {}", cached.get());
            return Mono.just(cached);
        }

        // Validate with Operator Service and cache result
        return operatorServiceClient.validateApiKey(apiKey)
            .doOnNext(operatorIdOpt -> {
                if (operatorIdOpt.isPresent()) {
                    apiKeyCache.put(apiKey, operatorIdOpt.get());
                } else {
                    log.warn("API key validation failed: {}", maskApiKey(apiKey));
                }
            })
            .onErrorResume(ex -> {
                log.error("Error validating API key: {}", ex.getMessage(), ex);
                return Mono.just(Optional.<Long>empty());
            });
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        return apiKey.substring(0, 7) + "***" + apiKey.substring(apiKey.length() - 4);
    }
}
