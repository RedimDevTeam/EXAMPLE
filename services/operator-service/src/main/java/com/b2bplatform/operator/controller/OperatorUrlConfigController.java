package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateUrlConfigRequest;
import com.b2bplatform.operator.dto.request.UpdateUrlConfigRequest;
import com.b2bplatform.operator.dto.response.UrlConfigResponse;
import com.b2bplatform.operator.service.OperatorUrlConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for managing operator URL configuration.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/url-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator URL Configuration", description = "APIs for managing operator URL configuration")
public class OperatorUrlConfigController {
    
    private final OperatorUrlConfigService urlConfigService;
    
    @GetMapping
    @Operation(summary = "Get URL config", description = "Retrieve URL configuration for an operator")
    public ResponseEntity<UrlConfigResponse> getUrlConfig(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/url-config", operatorId);
        
        Optional<UrlConfigResponse> config = urlConfigService.getUrlConfigByOperatorId(operatorId);
        return config.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create URL config", description = "Create URL configuration for an operator")
    public ResponseEntity<UrlConfigResponse> createUrlConfig(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateUrlConfigRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/url-config", operatorId);
        
        UrlConfigResponse created = urlConfigService.createUrlConfig(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping
    @Operation(summary = "Update URL config", description = "Update URL configuration for an operator")
    public ResponseEntity<UrlConfigResponse> updateUrlConfig(
            @PathVariable Long operatorId,
            @Valid @RequestBody UpdateUrlConfigRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/url-config", operatorId);
        
        UrlConfigResponse updated = urlConfigService.updateUrlConfig(operatorId, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping
    @Operation(summary = "Delete URL config", description = "Delete URL configuration for an operator")
    public ResponseEntity<Void> deleteUrlConfig(@PathVariable Long operatorId) {
        log.debug("DELETE /api/v1/admin/operators/{}/url-config", operatorId);
        
        urlConfigService.deleteUrlConfig(operatorId);
        return ResponseEntity.noContent().build();
    }
}
