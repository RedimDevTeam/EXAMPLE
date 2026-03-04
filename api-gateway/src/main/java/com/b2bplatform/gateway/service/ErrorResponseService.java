package com.b2bplatform.gateway.service;

import com.b2bplatform.gateway.dto.response.ErrorResponse;
import com.b2bplatform.gateway.dto.response.FallbackResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Service for building standardized error responses.
 * Centralizes error response creation logic.
 */
@Service
@Slf4j
public class ErrorResponseService {
    
    // Thread-safe ObjectMapper instance
    private static final ObjectMapper objectMapper;
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        objectMapper = mapper;
    }
    
    /**
     * Build a standardized error response DTO.
     */
    public ErrorResponse buildErrorResponse(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(path)
            .build();
    }
    
    /**
     * Build a fallback response DTO for circuit breaker scenarios.
     */
    public FallbackResponse buildFallbackResponse(String serviceName, String path) {
        return FallbackResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error("Service Unavailable")
            .message(String.format("%s service is currently unavailable. Please try again later.", serviceName))
            .service(serviceName)
            .path(path)
            .build();
    }
    
    /**
     * Convert ErrorResponse DTO to JSON string for reactive responses.
     */
    public String toJson(ErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializing ErrorResponse to JSON: {}", e.getMessage(), e);
            // Fallback to manual JSON construction
            return String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                errorResponse.getTimestamp(),
                errorResponse.getStatus(),
                escapeJson(errorResponse.getError()),
                escapeJson(errorResponse.getMessage()),
                escapeJson(errorResponse.getPath())
            );
        }
    }
    
    /**
     * Convert FallbackResponse DTO to JSON string for reactive responses.
     */
    public String toJson(FallbackResponse fallbackResponse) {
        try {
            return objectMapper.writeValueAsString(fallbackResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializing FallbackResponse to JSON: {}", e.getMessage(), e);
            // Fallback to manual JSON construction
            return String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"service\":\"%s\",\"path\":\"%s\"}",
                fallbackResponse.getTimestamp(),
                fallbackResponse.getStatus(),
                escapeJson(fallbackResponse.getError()),
                escapeJson(fallbackResponse.getMessage()),
                escapeJson(fallbackResponse.getService()),
                escapeJson(fallbackResponse.getPath())
            );
        }
    }
    
    /**
     * Convert ErrorResponse to bytes for reactive response writing.
     */
    public byte[] toBytes(ErrorResponse errorResponse) {
        return toJson(errorResponse).getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Convert FallbackResponse to bytes for reactive response writing.
     */
    public byte[] toBytes(FallbackResponse fallbackResponse) {
        return toJson(fallbackResponse).getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Escape JSON string values to prevent injection.
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
