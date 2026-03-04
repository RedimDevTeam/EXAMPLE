package com.b2bplatform.gateway.controller;

import com.b2bplatform.gateway.dto.response.FallbackResponse;
import com.b2bplatform.gateway.service.ErrorResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback controller for circuit breaker failures.
 * Returns standardized fallback responses when downstream services are unavailable.
 */
@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
@Slf4j
public class FallbackController {
    
    private final ErrorResponseService errorResponseService;
    
    @GetMapping("/operator")
    public ResponseEntity<FallbackResponse> operatorFallback() {
        log.warn("Circuit breaker fallback triggered for Operator Service");
        
        FallbackResponse response = errorResponseService.buildFallbackResponse(
            "Operator",
            "/fallback/operator"
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
