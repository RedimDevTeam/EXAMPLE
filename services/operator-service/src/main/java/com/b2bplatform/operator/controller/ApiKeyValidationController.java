package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.service.OperatorApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for API key validation (used by Gateway)
 */
@RestController
@RequestMapping("/api/v1/internal/api-keys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Key Validation", description = "Internal API for validating API keys (Gateway use)")
public class ApiKeyValidationController {
    
    private final OperatorApiKeyService apiKeyService;
    
    @GetMapping("/validate")
    @Operation(summary = "Validate API key", description = "Internal endpoint for Gateway to validate API keys")
    public ResponseEntity<Map<String, Object>> validateApiKey(@RequestParam String apiKey) {
        log.debug("Validating API key: {}", maskApiKey(apiKey));
        
        Optional<Long> operatorIdOpt = apiKeyService.validateApiKey(apiKey);
        
        Map<String, Object> response = new HashMap<>();
        
        if (operatorIdOpt.isPresent()) {
            response.put("valid", true);
            response.put("operatorId", operatorIdOpt.get());
            log.debug("API key validation successful for operator: {}", operatorIdOpt.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Invalid or inactive API key");
            log.debug("API key validation failed");
            return ResponseEntity.status(401).body(response);
        }
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        return apiKey.substring(0, 7) + "***" + apiKey.substring(apiKey.length() - 4);
    }
}
