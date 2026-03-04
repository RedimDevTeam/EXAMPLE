package com.b2bplatform.gateway.filter;

import com.b2bplatform.gateway.dto.response.ErrorResponse;
import com.b2bplatform.gateway.service.ErrorResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global error handler for Gateway exceptions
 */
@Component
@Order(-2)
@Slf4j
@RequiredArgsConstructor
public class GlobalGatewayErrorHandler implements ErrorWebExceptionHandler {
    
    private final ErrorResponseService errorResponseService;
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error for path {}: {}", exchange.getRequest().getURI().getPath(), ex.getMessage(), ex);
        log.error("Exception type: {}", ex.getClass().getName());
        if (ex.getCause() != null) {
            log.error("Caused by: {}", ex.getCause().getMessage(), ex.getCause());
        }
        
        ServerHttpResponse response = exchange.getResponse();
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal Server Error";
        
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            status = HttpStatus.BAD_GATEWAY;
            message = "Service unavailable. Please check if the target service is running.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("timeout")) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Request timeout";
        }
        
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
        return response.writeWith(Mono.just(buffer));
    }
}
