package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateApiCredentialsRequest;
import com.b2bplatform.operator.dto.request.UpdateApiCredentialsRequest;
import com.b2bplatform.operator.dto.response.ApiCredentialsResponse;
import com.b2bplatform.operator.service.ApiCredentialsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing API access credentials (username/password).
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/api-credentials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Credentials", description = "APIs for managing username/password authentication (Gaming Provider Global Admins only)")
public class ApiCredentialsController {
    
    private final ApiCredentialsService apiCredentialsService;
    
    @PostMapping
    @Operation(summary = "Create API credentials", 
               description = "Create username/password credentials for operator API access")
    public ResponseEntity<ApiCredentialsResponse> createCredentials(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateApiCredentialsRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/api-credentials", operatorId);
        
        ApiCredentialsResponse created = apiCredentialsService.createCredentials(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Get API credentials", 
               description = "Get API credentials for an operator")
    public ResponseEntity<ApiCredentialsResponse> getCredentials(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/api-credentials", operatorId);
        
        ApiCredentialsResponse credentials = apiCredentialsService.getCredentials(operatorId);
        return ResponseEntity.ok(credentials);
    }
    
    @PutMapping
    @Operation(summary = "Update API credentials", 
               description = "Update password or active status for API credentials")
    public ResponseEntity<ApiCredentialsResponse> updateCredentials(
            @PathVariable Long operatorId,
            @Valid @RequestBody UpdateApiCredentialsRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/api-credentials", operatorId);
        
        ApiCredentialsResponse updated = apiCredentialsService.updateCredentials(operatorId, request);
        return ResponseEntity.ok(updated);
    }
}
