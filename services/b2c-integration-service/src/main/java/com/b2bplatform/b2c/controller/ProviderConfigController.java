package com.b2bplatform.b2c.controller;

import com.b2bplatform.b2c.dto.request.CreateProviderConfigRequest;
import com.b2bplatform.b2c.dto.request.UpdateProviderConfigRequest;
import com.b2bplatform.b2c.dto.response.ProviderConfigResponse;
import com.b2bplatform.b2c.service.ProviderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for B2C Provider Configuration Management
 * 
 * Base Path: /api/v1/b2c/providers
 */
@RestController
@RequestMapping("/api/v1/b2c/providers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "B2C Provider Configuration", description = "Manage B2C provider configurations")
public class ProviderConfigController {
    
    private final ProviderConfigService providerConfigService;
    
    /**
     * Create a new provider configuration
     */
    @PostMapping
    @Operation(summary = "Create provider configuration", description = "Create a new B2C provider configuration")
    public ResponseEntity<ProviderConfigResponse> createProvider(
            @Valid @RequestBody CreateProviderConfigRequest request) {
        log.info("POST /api/v1/b2c/providers - Creating provider: {}", request.getProviderId());
        
        ProviderConfigResponse response = providerConfigService.createProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get provider configuration by provider ID
     */
    @GetMapping("/{providerId}")
    @Operation(summary = "Get provider configuration", description = "Get provider configuration by provider ID")
    public ResponseEntity<ProviderConfigResponse> getProvider(@PathVariable String providerId) {
        log.debug("GET /api/v1/b2c/providers/{}", providerId);
        
        ProviderConfigResponse response = providerConfigService.getProvider(providerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update provider configuration
     */
    @PutMapping("/{providerId}")
    @Operation(summary = "Update provider configuration", description = "Update an existing provider configuration")
    public ResponseEntity<ProviderConfigResponse> updateProvider(
            @PathVariable String providerId,
            @Valid @RequestBody UpdateProviderConfigRequest request) {
        log.info("PUT /api/v1/b2c/providers/{} - Updating provider", providerId);
        
        ProviderConfigResponse response = providerConfigService.updateProvider(providerId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete provider configuration
     */
    @DeleteMapping("/{providerId}")
    @Operation(summary = "Delete provider configuration", description = "Delete a provider configuration")
    public ResponseEntity<Void> deleteProvider(@PathVariable String providerId) {
        log.info("DELETE /api/v1/b2c/providers/{} - Deleting provider", providerId);
        
        providerConfigService.deleteProvider(providerId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * List all providers
     */
    @GetMapping
    @Operation(summary = "List providers", description = "List all provider configurations")
    public ResponseEntity<List<ProviderConfigResponse>> listProviders(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        log.debug("GET /api/v1/b2c/providers - Listing providers (activeOnly={})", activeOnly);
        
        List<ProviderConfigResponse> providers = activeOnly 
                ? providerConfigService.listActiveProviders()
                : providerConfigService.listProviders();
        
        return ResponseEntity.ok(providers);
    }
}
