package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.model.OperatorWalletConfig;
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
 * Client for calling operator webhooks for wallet operations
 */
@Service
@Slf4j
public class OperatorWebhookClient {
    
    private final WebClient webClient;
    
    @Value("${wallet.default-timeout-ms:5000}")
    private int defaultTimeoutMs;
    
    @Value("${wallet.default-retry-attempts:3}")
    private int defaultRetryAttempts;
    
    @Value("${wallet.retry-delay-ms:1000}")
    private long retryDelayMs;
    
    public OperatorWebhookClient() {
        this.webClient = WebClient.builder()
            .build();
    }
    
    /**
     * Call operator debit webhook
     */
    public Mono<Map<String, Object>> debit(OperatorWalletConfig config, Map<String, Object> request) {
        return callWebhook(config.getDebitUrl(), config, request, "DEBIT");
    }
    
    /**
     * Call operator credit webhook
     */
    public Mono<Map<String, Object>> credit(OperatorWalletConfig config, Map<String, Object> request) {
        return callWebhook(config.getCreditUrl(), config, request, "CREDIT");
    }
    
    /**
     * Call operator balance query webhook
     */
    public Mono<Map<String, Object>> balance(OperatorWalletConfig config, String playerId) {
        String url = config.getBalanceUrl() + "?playerId=" + playerId;
        return callWebhook(url, config, null, "BALANCE_QUERY", true);
    }
    
    /**
     * Call operator transfer webhook
     */
    public Mono<Map<String, Object>> transfer(OperatorWalletConfig config, Map<String, Object> request) {
        return callWebhook(config.getTransferUrl(), config, request, "TRANSFER");
    }
    
    /**
     * Generic webhook caller
     */
    private Mono<Map<String, Object>> callWebhook(String url, OperatorWalletConfig config, 
                                                  Map<String, Object> requestBody, String operation) {
        return callWebhook(url, config, requestBody, operation, false);
    }
    
    private Mono<Map<String, Object>> callWebhook(String url, OperatorWalletConfig config, 
                                                  Map<String, Object> requestBody, String operation, boolean isGet) {
        log.info("Calling operator webhook: {} - {}", operation, url);
        
        String requestId = UUID.randomUUID().toString();
        int timeout = config.getTimeoutMs() != null ? config.getTimeoutMs() : defaultTimeoutMs;
        int retries = config.getRetryAttempts() != null ? config.getRetryAttempts() : defaultRetryAttempts;
        
        WebClient.RequestHeadersSpec<?> finalSpec;
        
        if (isGet) {
            // GET request
            finalSpec = webClient.get()
                .uri(url)
                .header(config.getAuthHeader(), config.getAuthValue())
                .header("X-Request-ID", requestId)
                .accept(MediaType.APPLICATION_JSON);
        } else {
            // POST request
            WebClient.RequestBodySpec bodySpec = webClient.post()
                .uri(url)
                .header(config.getAuthHeader(), config.getAuthValue())
                .header("X-Request-ID", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
            
            if (requestBody != null) {
                finalSpec = bodySpec.bodyValue(requestBody);
            } else {
                finalSpec = bodySpec;
            }
        }
        
        return finalSpec
            .retrieve()
            .bodyToMono(Map.class)
            .cast(Map.class)
            .map(op -> (Map<String, Object>) op)
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
                    log.warn("Retrying webhook call: {} (attempt {})", url, retrySignal.totalRetries() + 1);
                }))
            .doOnSuccess(response -> {
                log.info("Webhook call successful: {} - {}", operation, url);
            })
            .doOnError(error -> {
                log.error("Webhook call failed: {} - {} - Error: {}", operation, url, error.getMessage());
            });
    }
}
