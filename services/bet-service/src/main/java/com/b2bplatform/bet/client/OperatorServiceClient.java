package com.b2bplatform.bet.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * Client for posting bets to Operator webhook for confirmation.
 * This is CRITICAL: Bet must be confirmed by operator BEFORE game starts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OperatorServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${operator.webhook.timeout:3000ms}")
    private Duration timeout;
    
    private WebClient getWebClient() {
        return webClientBuilder
                .build();
    }
    
    /**
     * Post bet to operator webhook for confirmation.
     * This MUST complete before game starts, otherwise bet will be auto-rejected.
     * 
     * @param operatorWebhookUrl Operator's bet confirmation webhook URL
     * @param betRequest Bet details to post
     * @return Operator response (success, low balance, not a player, etc.)
     */
    @CircuitBreaker(name = "operatorService", fallbackMethod = "postBetFallback")
    @TimeLimiter(name = "operatorService")
    public Mono<OperatorBetResponse> postBetToOperator(String operatorWebhookUrl, Map<String, Object> betRequest) {
        log.debug("Posting bet to operator webhook: {}", operatorWebhookUrl);
        
        return getWebClient()
                .post()
                .uri(operatorWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(betRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(response -> (Map<String, Object>) response)
                .timeout(timeout)
                .map(this::parseOperatorResponse)
                .doOnSuccess(response -> log.debug("Operator bet confirmation received: {}", response))
                .doOnError(error -> log.error("Operator bet confirmation failed: {}", error.getMessage()));
    }
    
    /**
     * Parse operator response into structured format
     */
    private OperatorBetResponse parseOperatorResponse(Map<String, Object> response) {
        Boolean success = (Boolean) response.getOrDefault("success", false);
        String message = (String) response.getOrDefault("message", "");
        String error = (String) response.getOrDefault("error", "");
        
        // Determine response type
        OperatorBetResponse.OperatorResponseType responseType;
        if (Boolean.TRUE.equals(success)) {
            responseType = OperatorBetResponse.OperatorResponseType.SUCCESS;
        } else if (error != null && !error.isEmpty()) {
            // Check for specific error types
            String errorLower = error.toLowerCase();
            if (errorLower.contains("balance") || errorLower.contains("insufficient")) {
                responseType = OperatorBetResponse.OperatorResponseType.LOW_BALANCE;
            } else if (errorLower.contains("player") || errorLower.contains("not found")) {
                responseType = OperatorBetResponse.OperatorResponseType.NOT_A_PLAYER;
            } else {
                responseType = OperatorBetResponse.OperatorResponseType.REJECTED;
            }
        } else {
            responseType = OperatorBetResponse.OperatorResponseType.REJECTED;
        }
        
        return OperatorBetResponse.builder()
                .success(success != null && success)
                .responseType(responseType)
                .message(message != null ? message : error)
                .responsePayload(response)
                .build();
    }
    
    private Mono<OperatorBetResponse> postBetFallback(String operatorWebhookUrl, Map<String, Object> betRequest, Exception ex) {
        log.error("Operator bet confirmation circuit breaker fallback: {}", ex.getMessage());
        return Mono.just(OperatorBetResponse.builder()
                .success(false)
                .responseType(OperatorBetResponse.OperatorResponseType.TIMEOUT)
                .message("Operator webhook timeout: " + ex.getMessage())
                .build());
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OperatorBetResponse {
        private Boolean success;
        private OperatorResponseType responseType;
        private String message;
        private Map<String, Object> responsePayload;
        
        public enum OperatorResponseType {
            SUCCESS,           // Bet confirmed successfully
            LOW_BALANCE,        // Operator rejected: low balance
            NO_BALANCE,         // Operator rejected: no balance
            NOT_A_PLAYER,       // Operator rejected: not a player
            REJECTED,           // Operator rejected: other reason
            TIMEOUT             // No response from operator (timeout)
        }
    }
}
