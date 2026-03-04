package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateCommissionConfigRequest;
import com.b2bplatform.operator.dto.request.UpdateCommissionConfigRequest;
import com.b2bplatform.operator.dto.response.CommissionCalculationResponse;
import com.b2bplatform.operator.dto.response.CommissionConfigResponse;
import com.b2bplatform.operator.service.CommissionConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing commission configurations.
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/commission-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commission Configuration", description = "APIs for managing commission models (Gaming Provider Global Admins only)")
public class CommissionConfigController {
    
    private final CommissionConfigService commissionConfigService;
    
    @PostMapping
    @Operation(summary = "Create commission configuration", 
               description = "Create a new commission configuration for an operator (Gaming Provider Global Admin only)")
    public ResponseEntity<CommissionConfigResponse> createCommissionConfig(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateCommissionConfigRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/commission-config", operatorId);
        
        CommissionConfigResponse created = commissionConfigService.createCommissionConfig(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Get commission configurations", 
               description = "Retrieve commission configurations for an operator")
    public ResponseEntity<List<CommissionConfigResponse>> getCommissionConfigs(
            @PathVariable Long operatorId,
            @Parameter(description = "Filter by game provider ID") @RequestParam(required = false) String gameProviderId,
            @Parameter(description = "Filter by game ID") @RequestParam(required = false) String gameId,
            @Parameter(description = "Only return active configurations") @RequestParam(defaultValue = "true") Boolean activeOnly) {
        log.debug("GET /api/v1/admin/operators/{}/commission-config", operatorId);
        
        List<CommissionConfigResponse> configs = commissionConfigService.getCommissionConfigs(
            operatorId, gameProviderId, gameId, activeOnly);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/{configId}")
    @Operation(summary = "Update commission configuration", 
               description = "Update an existing commission configuration")
    public ResponseEntity<CommissionConfigResponse> updateCommissionConfig(
            @PathVariable Long operatorId,
            @PathVariable Long configId,
            @Valid @RequestBody UpdateCommissionConfigRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/commission-config/{}", operatorId, configId);
        
        CommissionConfigResponse updated = commissionConfigService.updateCommissionConfig(operatorId, configId, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{configId}")
    @Operation(summary = "Deactivate commission configuration", 
               description = "Deactivate a commission configuration")
    public ResponseEntity<CommissionConfigResponse> deactivateCommissionConfig(
            @PathVariable Long operatorId,
            @PathVariable Long configId,
            @Parameter(description = "Effective end date") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime effectiveTo,
            @Parameter(description = "Reason for deactivation") @RequestParam(required = false) String reason) {
        log.debug("DELETE /api/v1/admin/operators/{}/commission-config/{}", operatorId, configId);
        
        CommissionConfigResponse deactivated = commissionConfigService.deactivateCommissionConfig(
            operatorId, configId, effectiveTo, reason);
        return ResponseEntity.ok(deactivated);
    }
    
    @GetMapping("/calculations")
    @Operation(summary = "Get commission calculations", 
               description = "Retrieve commission calculation history")
    public ResponseEntity<Page<CommissionCalculationResponse>> getCommissionCalculations(
            @PathVariable Long operatorId,
            @Parameter(description = "Filter by game provider ID") @RequestParam(required = false) String gameProviderId,
            @Parameter(description = "Calculation period start date") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Calculation period end date") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Filter by settlement cycle ID") @RequestParam(required = false) Long settlementCycleId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v1/admin/operators/{}/commission-config/calculations", operatorId);
        
        Page<CommissionCalculationResponse> calculations = commissionConfigService.getCommissionCalculations(
            operatorId, gameProviderId, startDate, endDate, settlementCycleId, page, size);
        return ResponseEntity.ok(calculations);
    }
}
