package com.b2bplatform.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Filter for JWT token authentication
 * Validates JWT tokens for player endpoints
 */
@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
    
    @Value("${jwt.secret:your-secret-key-change-in-production-min-256-bits}")
    private String jwtSecret;
    
    public JwtAuthFilter() {
        super(Config.class);
    }
    
    @Override
    public String name() {
        return "JwtAuth";
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
                return applyFilter(exchange, chain);
            }
        };
    }
    
    private Mono<Void> applyFilter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        try {
            // Validate JWT token
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            // Extract claims
            String playerId = claims.getSubject();
            String operatorId = claims.get("operator_id", String.class);
            
            // Add user info to headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Player-Id", playerId != null ? playerId : "")
                .header("X-Operator-Id", operatorId != null ? operatorId : "")
                .build();
            
            log.debug("JWT validated for player: {}, operator: {}", playerId, operatorId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(new IllegalStateException("Response already committed"));
        }
        
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        
        String errorBody = String.format(
            "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
            java.time.LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            exchange.getRequest().getURI().getPath()
        );
        
        byte[] bytes = errorBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer)).doOnError(ex -> {
            log.error("Error writing error response: {}", ex.getMessage(), ex);
        });
    }
    
    public static class Config {
        // Configuration properties if needed
    }
}
