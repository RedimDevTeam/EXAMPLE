package com.b2bplatform.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Client for calling Operator Service to validate API keys.
 * Uses Eureka discovery (lb://operator-service) when available.
 */
@Service
@Slf4j
public class OperatorServiceClient {

    private final WebClient webClient;

    public OperatorServiceClient(WebClient.Builder loadBalancedWebClientBuilder,
                                @Value("${operator.service.url:lb://operator-service}") String operatorServiceUrl) {
        this.webClient = loadBalancedWebClientBuilder
            .baseUrl(operatorServiceUrl)
            .build();
        log.info("OperatorServiceClient initialized with URL: {}", operatorServiceUrl);
    }
    
    /**
     * Validate API key with Operator Service
     * Returns operator ID if valid, empty if invalid
     */
    public Mono<Optional<Long>> validateApiKey(String apiKey) {
        log.debug("Calling Operator Service to validate API key");
        
        return webClient.get()
            .uri("/api/v1/internal/api-keys/validate?apiKey={apiKey}", apiKey)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofSeconds(2))
            .map(response -> {
                Boolean valid = (Boolean) response.get("valid");
                if (Boolean.TRUE.equals(valid)) {
                    Number operatorId = (Number) response.get("operatorId");
                    return Optional.of(operatorId.longValue());
                }
                return Optional.<Long>empty();
            })
            .onErrorResume(ex -> {
                log.error("Error validating API key with Operator Service: {}", ex.getMessage());
                // On error, return empty (fail closed - don't allow access)
                return Mono.just(Optional.<Long>empty());
            })
            .defaultIfEmpty(Optional.<Long>empty());
    }
}
