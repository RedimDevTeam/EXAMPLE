package com.b2bplatform.gateway.filter;

import com.b2bplatform.gateway.dto.response.ErrorResponse;
import com.b2bplatform.gateway.service.ApiKeyValidationService;
import com.b2bplatform.gateway.service.ErrorResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Filter for API Key authentication
 * Validates API keys with Operator Service (with Redis caching)
 * 
 * Usage in application.yml:
 * filters:
 *   - name: ApiKeyAuth
 */
@Component
@Slf4j
public class ApiKeyAuthFilter extends AbstractGatewayFilterFactory<ApiKeyAuthFilter.Config> {
    
    private final ApiKeyValidationService validationService;
    private final ErrorResponseService errorResponseService;
    
    public ApiKeyAuthFilter(ApiKeyValidationService validationService,
                           ErrorResponseService errorResponseService) {
        super(Config.class);
        this.validationService = validationService;
        this.errorResponseService = errorResponseService;
    }
    
    @Override
    public String name() {
        return "ApiKeyAuth";
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
        String apiKey = request.getHeaders().getFirst("X-API-Key");
        
        log.debug("ApiKeyAuth filter processing request: {}", request.getURI());
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Missing API key for request: {}", request.getURI());
            return onError(exchange, "Missing API key", HttpStatus.UNAUTHORIZED);
        }
        
        // Delegate validation to service layer
        return validationService.validateApiKey(apiKey)
            .flatMap(operatorIdOpt -> {
                if (operatorIdOpt.isPresent()) {
                    Long operatorId = operatorIdOpt.get();
                    return proceedWithRequest(exchange, chain, operatorId);
                } else {
                    log.warn("API key validation failed: {}", maskApiKey(apiKey));
                    return onError(exchange, "Invalid or inactive API key", HttpStatus.UNAUTHORIZED);
                }
            })
            .onErrorResume(ex -> {
                log.error("Error in API key validation: {}", ex.getMessage(), ex);
                return onError(exchange, "Authentication error", HttpStatus.UNAUTHORIZED);
            });
    }
    
    private Mono<Void> proceedWithRequest(ServerWebExchange exchange, 
                                         org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
                                         Long operatorId) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-Operator-Id", operatorId.toString())
            .build();
        
        log.debug("API key validated for operator: {}, forwarding request", operatorId);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(new IllegalStateException("Response already committed"));
        }
        
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // Use ErrorResponseService to build standardized error response
        ErrorResponse errorResponse = errorResponseService.buildErrorResponse(
            status,
            message,
            exchange.getRequest().getURI().getPath()
        );
        
        byte[] bytes = errorResponseService.toBytes(errorResponse);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer)).doOnError(ex -> {
            log.error("Error writing error response: {}", ex.getMessage());
        });
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        return apiKey.substring(0, 7) + "***" + apiKey.substring(apiKey.length() - 4);
    }
    
    public static class Config {
        // Configuration properties if needed
    }
}
