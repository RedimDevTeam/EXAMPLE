package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.MaintenanceRequest;
import com.b2bplatform.operator.dto.response.MaintenanceStatusResponse;
import com.b2bplatform.operator.service.OperatorMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing operator maintenance mode.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/maintenance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator Maintenance", description = "APIs for managing operator maintenance mode")
public class OperatorMaintenanceController {
    
    private final OperatorMaintenanceService maintenanceService;
    
    @GetMapping
    @Operation(summary = "Get maintenance status", description = "Retrieve maintenance status for an operator")
    public ResponseEntity<MaintenanceStatusResponse> getMaintenanceStatus(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/maintenance", operatorId);
        
        MaintenanceStatusResponse status = maintenanceService.getMaintenanceStatus(operatorId);
        return ResponseEntity.ok(status);
    }
    
    @PostMapping
    @Operation(summary = "Set maintenance mode", description = "Enable or disable maintenance mode for an operator")
    public ResponseEntity<MaintenanceStatusResponse> setMaintenanceMode(
            @PathVariable Long operatorId,
            @Valid @RequestBody MaintenanceRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/maintenance", operatorId);
        
        MaintenanceStatusResponse status = maintenanceService.setMaintenanceMode(operatorId, request);
        return ResponseEntity.ok(status);
    }
}
