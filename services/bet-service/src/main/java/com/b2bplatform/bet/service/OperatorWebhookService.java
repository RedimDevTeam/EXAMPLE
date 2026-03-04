package com.b2bplatform.bet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Service to get operator bet webhook URL from Wallet Service.
 * Operators provide webhook URLs for bet confirmation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorWebhookService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${wallet.service.url:http://localhost:8083}")
    private String walletServiceUrl;
    
    /**
     * Get operator bet webhook URL from Wallet Service config.
     * For now, we'll use the debit URL as bet confirmation URL.
     * In production, operators should provide a dedicated bet confirmation webhook.
     */
    public Mono<String> getOperatorBetWebhookUrl(Long operatorId) {
        log.debug("Getting operator bet webhook URL for operator: {}", operatorId);
        
        WebClient webClient = webClientBuilder.build();
        
        // Call Wallet Service to get operator wallet config
        // In production, operators should have a dedicated bet confirmation webhook URL
        return webClient.get()
                .uri(walletServiceUrl + "/api/v1/wallet/config/operator/{operatorId}", operatorId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(response -> (Map<String, Object>) response)
                .map(config -> {
                    // Use debit URL as bet confirmation URL (operators should provide dedicated URL)
                    String debitUrl = (String) config.get("debitUrl");
                    if (debitUrl == null || debitUrl.isEmpty()) {
                        throw new IllegalStateException("Operator bet webhook URL not configured");
                    }
                    log.debug("Operator bet webhook URL: {}", debitUrl);
                    return debitUrl;
                })
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(ex -> {
                    log.error("Error getting operator bet webhook URL: {}", ex.getMessage());
                    return Mono.error(new IllegalStateException("Failed to get operator bet webhook URL: " + ex.getMessage()));
                });
    }
}
