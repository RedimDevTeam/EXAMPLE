package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.model.OperatorApiKey;
import com.b2bplatform.operator.service.OperatorApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/operators/{operatorId}/api-keys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator API Key Management", description = "APIs for managing operator API keys")
public class OperatorApiKeyController {
    
    private final OperatorApiKeyService apiKeyService;
    
    @PostMapping
    @Operation(summary = "Create API key", description = "Create a new API key for an operator")
    public ResponseEntity<OperatorApiKey> createApiKey(
            @PathVariable Long operatorId,
            @RequestParam(required = false) String keyName) {
        log.debug("POST /api/v1/operators/{}/api-keys - Creating API key", operatorId);
        try {
            OperatorApiKey apiKey = apiKeyService.createApiKey(operatorId, keyName);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiKey);
        } catch (IllegalArgumentException e) {
            log.error("Error creating API key: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all API keys", description = "Get all API keys for an operator")
    public ResponseEntity<List<OperatorApiKey>> getApiKeys(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/operators/{}/api-keys - Getting API keys", operatorId);
        return ResponseEntity.ok(apiKeyService.getApiKeysByOperator(operatorId));
    }
    
    @PostMapping("/{apiKey}/revoke")
    @Operation(summary = "Revoke API key", description = "Revoke an API key")
    public ResponseEntity<Void> revokeApiKey(@PathVariable String apiKey) {
        log.debug("POST /api/v1/operators/{}/api-keys/{}/revoke - Revoking API key", apiKey);
        apiKeyService.revokeApiKey(apiKey);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{apiKey}/status")
    @Operation(summary = "Update API key status", description = "Activate or deactivate an API key")
    public ResponseEntity<Void> updateApiKeyStatus(
            @PathVariable String apiKey,
            @RequestParam String status) {
        log.debug("PUT /api/v1/operators/{}/api-keys/{}/status - Updating status to {}", apiKey, status);
        apiKeyService.updateApiKeyStatus(apiKey, status);
        return ResponseEntity.noContent().build();
    }
}
