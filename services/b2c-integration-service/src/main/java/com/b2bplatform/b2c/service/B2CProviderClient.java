package com.b2bplatform.b2c.service;

import com.b2bplatform.b2c.model.ProviderConfig;
import com.b2bplatform.b2c.util.SignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP client for calling B2C provider APIs
 * Handles authentication, retries, and error handling
 */
@Service
@Slf4j
public class B2CProviderClient {
    
    private final WebClient webClient;
    private final SignatureService signatureService;
    private final ObjectMapper objectMapper;
    
    @Value("${b2c.provider.default-timeout-ms:5000}")
    private int defaultTimeoutMs;
    
    @Value("${b2c.provider.default-retry-attempts:3}")
    private int defaultRetryAttempts;
    
    @Value("${b2c.provider.retry-delay-ms:1000}")
    private long retryDelayMs;
    
    public B2CProviderClient(SignatureService signatureService, ObjectMapper objectMapper) {
        this.webClient = WebClient.builder().build();
        this.signatureService = signatureService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Call provider API endpoint
     * 
     * @param providerConfig Provider configuration
     * @param endpoint API endpoint path (e.g., "/wallet/debit")
     * @param requestBody Request body object (will be serialized to JSON)
     * @param operation Operation name for logging (e.g., "DEBIT", "CREDIT")
     * @return Mono with provider response as Map
     */
    public Mono<Map<String, Object>> callProviderApi(
            ProviderConfig providerConfig,
            String endpoint,
            Object requestBody,
            String operation) {
        
        String url = providerConfig.getApiBaseUrl() + endpoint;
        log.info("Calling provider API: {} - {} - {}", operation, providerConfig.getProviderId(), url);
        
        String requestId = UUID.randomUUID().toString();
        int timeout = providerConfig.getTimeoutMs() != null ? providerConfig.getTimeoutMs() : defaultTimeoutMs;
        int retries = providerConfig.getRetryAttempts() != null ? providerConfig.getRetryAttempts() : defaultRetryAttempts;
        
        try {
            // Serialize request body to JSON
            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            
            // Generate signature if HMAC auth is used
            String signature = null;
            if (providerConfig.getAuthType() == ProviderConfig.AuthType.HMAC) {
                if (providerConfig.getApiSecret() == null || providerConfig.getApiSecret().isBlank()) {
                    throw new IllegalArgumentException("API secret is required for HMAC authentication");
                }
                signature = signatureService.generateJsonSignature(jsonPayload, providerConfig.getApiSecret());
            }
            
            // Build request
            WebClient.RequestBodySpec bodySpec = webClient.post()
                    .uri(url)
                    .header("X-Request-ID", requestId)
                    .header("X-Provider-Id", providerConfig.getProviderId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
            
            // Add authentication header
            switch (providerConfig.getAuthType()) {
                case API_KEY:
                    bodySpec = bodySpec.header("X-API-Key", providerConfig.getApiKey());
                    break;
                case HMAC:
                    bodySpec = bodySpec.header("X-Signature", signature);
                    if (providerConfig.getApiKey() != null) {
                        bodySpec = bodySpec.header("X-API-Key", providerConfig.getApiKey());
                    }
                    break;
                case OAUTH:
                    // OAuth implementation would go here
                    bodySpec = bodySpec.header("Authorization", "Bearer " + providerConfig.getApiKey());
                    break;
            }
            
            // Execute request with retry logic
            return bodySpec
                    .bodyValue(jsonPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .cast(Map.class)
                    .map(response -> (Map<String, Object>) response)
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.backoff(retries, Duration.ofMillis(retryDelayMs))
                            .filter(throwable -> {
                                // Retry on network errors and 5xx errors
                                if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                                    org.springframework.web.reactive.function.client.WebClientResponseException ex =
                                            (org.springframework.web.reactive.function.client.WebClientResponseException) throwable;
                                    return ex.getStatusCode().is5xxServerError();
                                }
                                return true; // Retry on other errors (network, timeout)
                            })
                            .doBeforeRetry(retrySignal -> {
                                log.warn("Retrying provider API call: {} - {} (attempt {})", 
                                    operation, url, retrySignal.totalRetries() + 1);
                            }))
                    .doOnSuccess(response -> {
                        log.info("Provider API call successful: {} - {} - {}", 
                            operation, providerConfig.getProviderId(), url);
                    })
                    .doOnError(error -> {
                        log.error("Provider API call failed: {} - {} - {} - Error: {}", 
                            operation, providerConfig.getProviderId(), url, error.getMessage());
                    });
                    
        } catch (Exception e) {
            log.error("Error preparing provider API call: {} - {}", operation, e.getMessage(), e);
            return Mono.error(new RuntimeException("Failed to call provider API: " + e.getMessage(), e));
        }
    }
    
    /**
     * Call provider API for GET requests (e.g., balance query)
     */
    public Mono<Map<String, Object>> callProviderApiGet(
            ProviderConfig providerConfig,
            String endpoint,
            String operation) {
        
        String url = providerConfig.getApiBaseUrl() + endpoint;
        log.info("Calling provider API (GET): {} - {} - {}", operation, providerConfig.getProviderId(), url);
        
        String requestId = UUID.randomUUID().toString();
        int timeout = providerConfig.getTimeoutMs() != null ? providerConfig.getTimeoutMs() : defaultTimeoutMs;
        int retries = providerConfig.getRetryAttempts() != null ? providerConfig.getRetryAttempts() : defaultRetryAttempts;
        
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                .uri(url)
                .header("X-Request-ID", requestId)
                .header("X-Provider-Id", providerConfig.getProviderId())
                .accept(MediaType.APPLICATION_JSON);
        
        // Add authentication header
        switch (providerConfig.getAuthType()) {
            case API_KEY:
                requestSpec = requestSpec.header("X-API-Key", providerConfig.getApiKey());
                break;
            case HMAC:
                // For GET requests, signature might be based on query params or timestamp
                // This is provider-specific, so we'll just use API key if available
                if (providerConfig.getApiKey() != null) {
                    requestSpec = requestSpec.header("X-API-Key", providerConfig.getApiKey());
                }
                break;
            case OAUTH:
                requestSpec = requestSpec.header("Authorization", "Bearer " + providerConfig.getApiKey());
                break;
        }
        
        return requestSpec
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(response -> (Map<String, Object>) response)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(retries, Duration.ofMillis(retryDelayMs))
                        .filter(throwable -> {
                            if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                                org.springframework.web.reactive.function.client.WebClientResponseException ex =
                                        (org.springframework.web.reactive.function.client.WebClientResponseException) throwable;
                                return ex.getStatusCode().is5xxServerError();
                            }
                            return true;
                        })
                        .doBeforeRetry(retrySignal -> {
                            log.warn("Retrying provider API call (GET): {} - {} (attempt {})", 
                                operation, url, retrySignal.totalRetries() + 1);
                        }))
                .doOnSuccess(response -> {
                    log.info("Provider API call successful (GET): {} - {} - {}", 
                        operation, providerConfig.getProviderId(), url);
                })
                .doOnError(error -> {
                    log.error("Provider API call failed (GET): {} - {} - {} - Error: {}", 
                        operation, providerConfig.getProviderId(), url, error.getMessage());
                });
    }
}
