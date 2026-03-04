package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.response.CommissionConfigResponse;
import com.b2bplatform.operator.service.CommissionConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Internal controller for commission configuration.
 * Used by Settlement Service to retrieve active commission configurations.
 */
@RestController
@RequestMapping("/api/v1/internal/operators/{operatorId}/commission-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commission Configuration (Internal)", description = "Internal APIs for retrieving commission configurations")
public class CommissionConfigInternalController {
    
    private final CommissionConfigService commissionConfigService;
    
    @GetMapping("/{gameProviderId}/{gameId}")
    @Operation(summary = "Get active commission configuration", 
               description = "Internal endpoint for Settlement Service to get active commission configuration")
    public ResponseEntity<CommissionConfigResponse> getActiveCommissionConfig(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @PathVariable String gameId) {
        log.debug("GET /api/v1/internal/operators/{}/commission-config/{}/{}", operatorId, gameProviderId, gameId);
        
        Optional<CommissionConfigResponse> config = commissionConfigService.getActiveCommissionConfig(
            operatorId, gameProviderId, gameId);
        return config.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{gameProviderId}")
    @Operation(summary = "Get active commission configuration (provider-level)", 
               description = "Internal endpoint to get provider-level commission configuration")
    public ResponseEntity<CommissionConfigResponse> getActiveCommissionConfigByProvider(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId) {
        log.debug("GET /api/v1/internal/operators/{}/commission-config/{}", operatorId, gameProviderId);
        
        Optional<CommissionConfigResponse> config = commissionConfigService.getActiveCommissionConfig(
            operatorId, gameProviderId, null);
        return config.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
