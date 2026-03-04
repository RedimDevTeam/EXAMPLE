package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.service.OperatorMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal controller for runtime services to check maintenance status.
 * Used by Bet Service, Wallet Service, Session Service, etc.
 */
@RestController
@RequestMapping("/api/v1/internal/maintenance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Maintenance Status (Internal)", description = "Internal APIs for checking operator maintenance status")
public class MaintenanceStatusController {
    
    private final OperatorMaintenanceService maintenanceService;
    
    @GetMapping("/operator/{operatorId}")
    @Operation(summary = "Check if operator is in maintenance", 
               description = "Internal endpoint for runtime services to check if an operator is in maintenance mode")
    public ResponseEntity<Map<String, Object>> checkMaintenanceStatus(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/internal/maintenance/operator/{}", operatorId);
        
        boolean isInMaintenance = maintenanceService.isInMaintenance(operatorId);
        
        return ResponseEntity.ok(Map.of(
            "operatorId", operatorId,
            "isInMaintenance", isInMaintenance
        ));
    }
}
