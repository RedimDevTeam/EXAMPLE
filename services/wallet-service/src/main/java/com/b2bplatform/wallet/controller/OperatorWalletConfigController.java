package com.b2bplatform.wallet.controller;

import com.b2bplatform.wallet.model.OperatorWalletConfig;
import com.b2bplatform.wallet.repository.OperatorWalletConfigRepository;
import com.b2bplatform.wallet.service.OperatorWalletConfigCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/wallet/config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator Wallet Configuration", description = "Manage operator wallet webhook URLs")
public class OperatorWalletConfigController {
    
    private final OperatorWalletConfigRepository configRepository;
    private final OperatorWalletConfigCacheService configCacheService;
    
    @PostMapping
    @Operation(summary = "Create wallet config", description = "Create wallet configuration for an operator")
    public ResponseEntity<OperatorWalletConfig> createConfig(@Valid @RequestBody OperatorWalletConfig config) {
        log.debug("POST /api/v1/wallet/config - operator: {}", config.getOperatorId());
        
        // Check if config already exists
        Optional<OperatorWalletConfig> existing = configRepository.findByOperatorId(config.getOperatorId());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        OperatorWalletConfig saved = configRepository.save(config);
        
        // Cache the new config in Redis
        configCacheService.cacheConfig(saved);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @GetMapping("/operator/{operatorId}")
    @Operation(summary = "Get wallet config", description = "Get wallet configuration for an operator")
    public ResponseEntity<OperatorWalletConfig> getConfig(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/wallet/config/operator/{}", operatorId);
        
        Optional<OperatorWalletConfig> config = configRepository.findByOperatorId(operatorId);
        
        if (config.isPresent()) {
            return ResponseEntity.ok(config.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/operator/{operatorId}")
    @Operation(summary = "Update wallet config", description = "Update wallet configuration for an operator")
    public ResponseEntity<OperatorWalletConfig> updateConfig(
            @PathVariable Long operatorId,
            @Valid @RequestBody OperatorWalletConfig config) {
        log.debug("PUT /api/v1/wallet/config/operator/{}", operatorId);
        
        Optional<OperatorWalletConfig> existing = configRepository.findByOperatorId(operatorId);
        
        if (existing.isPresent()) {
            OperatorWalletConfig existingConfig = existing.get();
            existingConfig.setDebitUrl(config.getDebitUrl());
            existingConfig.setCreditUrl(config.getCreditUrl());
            existingConfig.setBalanceUrl(config.getBalanceUrl());
            existingConfig.setTransferUrl(config.getTransferUrl());
            existingConfig.setAuthType(config.getAuthType());
            existingConfig.setAuthHeader(config.getAuthHeader());
            existingConfig.setAuthValue(config.getAuthValue());
            existingConfig.setTimeoutMs(config.getTimeoutMs());
            existingConfig.setRetryAttempts(config.getRetryAttempts());
            existingConfig.setEnabled(config.getEnabled());
            
            OperatorWalletConfig updated = configRepository.save(existingConfig);
            
            // Update cache in Redis
            configCacheService.cacheConfig(updated);
            
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    @Operation(summary = "List all configs", description = "Get all wallet configurations")
    public ResponseEntity<List<OperatorWalletConfig>> getAllConfigs() {
        log.debug("GET /api/v1/wallet/config");
        return ResponseEntity.ok(configRepository.findAll());
    }
}
