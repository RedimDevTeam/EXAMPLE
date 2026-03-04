package com.b2bplatform.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Synchronous client to create player session in session-service (DB + Redis).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${session.service.url:http://localhost:8085}")
    private String sessionServiceUrl;

    /**
     * Create session (sync). Returns result map (sessionId, etc.) from APIResponse.result, or null on failure.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createSession(CreateSessionRequestDto request) {
        try {
            WebClient client = webClientBuilder.baseUrl(sessionServiceUrl).build();
            Map<String, Object> body = client.post()
                .uri("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(2))
                .block();
            if (body != null && body.containsKey("result") && body.get("result") instanceof Map) {
                return (Map<String, Object>) body.get("result");
            }
            return null;
        } catch (Exception ex) {
            log.error("Error creating session: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * DTO for session create request (matches session-service CreateSessionRequest shape).
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateSessionRequestDto {
        private Long playerId;
        private Long operatorId;
        private String jwtToken;
        private String refreshToken;
        private String ipAddress;
        private String userAgent;
    }
}
