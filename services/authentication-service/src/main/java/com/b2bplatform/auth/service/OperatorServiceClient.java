package com.b2bplatform.auth.service;

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
 * Client for calling Operator Service (validate operators, validate API key).
 */
@Service
@Slf4j
public class OperatorServiceClient {

    private final WebClient webClient;

    public OperatorServiceClient(@Value("${operator.service.url:http://localhost:8081}") String operatorServiceUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(operatorServiceUrl)
            .build();
        log.info("OperatorServiceClient initialized with URL: {}", operatorServiceUrl);
    }

    /**
     * Validate API key (X-Api-Key). Returns operator ID if valid. Sync.
     */
    public Optional<Long> validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }
        try {
            Map<String, Object> response = webClient.get()
                .uri(uri -> uri.path("/api/v1/internal/api-keys/validate").queryParam("apiKey", apiKey).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(2))
                .block();
            if (response != null && Boolean.TRUE.equals(response.get("valid"))) {
                Object id = response.get("operatorId");
                if (id instanceof Number) {
                    return Optional.of(((Number) id).longValue());
                }
            }
        } catch (Exception ex) {
            log.error("Error validating API key: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Get operator by ID. Sync.
     */
    public Map<String, Object> getOperator(Long operatorId) {
        try {
            return webClient.get()
                .uri("/api/v1/operators/{id}", operatorId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(2))
                .block();
        } catch (Exception ex) {
            log.error("Error getting operator: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Validate operator exists and is active
     */
    public Mono<Boolean> validateOperator(Long operatorId) {
        log.debug("Validating operator: {}", operatorId);
        
        return webClient.get()
            .uri("/api/v1/operators/{id}", operatorId)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofSeconds(2))
            .map(response -> {
                String status = (String) response.get("status");
                return "ACTIVE".equals(status);
            })
            .onErrorResume(ex -> {
                log.error("Error validating operator: {}", ex.getMessage());
                return Mono.just(false);
            })
            .defaultIfEmpty(false);
    }
    
    /**
     * Get operator by code
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getOperatorByCode(String operatorCode) {
        log.debug("Getting operator by code: {}", operatorCode);
        
        return webClient.get()
            .uri("/api/v1/operators")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(Map.class)
            .filter(op -> operatorCode.equals(op.get("code")))
            .cast(Map.class)
            .map(op -> (Map<String, Object>) op)
            .next()
            .timeout(Duration.ofSeconds(2))
            .onErrorResume(ex -> {
                log.error("Error getting operator by code: {}", ex.getMessage());
                return Mono.<Map<String, Object>>empty();
            });
    }
}
