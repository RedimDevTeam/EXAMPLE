package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateReportAccessRequest;
import com.b2bplatform.operator.dto.request.UpdateReportAccessRequest;
import com.b2bplatform.operator.dto.response.ReportAccessResponse;
import com.b2bplatform.operator.service.ReportAccessService;
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

/**
 * Controller for managing report access (hierarchical roles).
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/report-access")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Report Access Management", description = "APIs for managing hierarchical report access roles (Gaming Provider Global Admins only)")
public class ReportAccessController {
    
    private final ReportAccessService reportAccessService;
    
    @PostMapping
    @Operation(summary = "Create report access", 
               description = "Create report access for a user with hierarchical role (CASINO_ADMIN, GROUP_ADMIN, GLOBAL_ADMIN)")
    public ResponseEntity<ReportAccessResponse> createReportAccess(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateReportAccessRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/report-access", operatorId);
        
        ReportAccessResponse created = reportAccessService.createReportAccess(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{userIdentifier}")
    @Operation(summary = "Get report access", 
               description = "Get report access for a specific user")
    public ResponseEntity<ReportAccessResponse> getReportAccess(
            @PathVariable Long operatorId,
            @PathVariable String userIdentifier) {
        log.debug("GET /api/v1/admin/operators/{}/report-access/{}", operatorId, userIdentifier);
        
        ReportAccessResponse access = reportAccessService.getReportAccess(operatorId, userIdentifier);
        return ResponseEntity.ok(access);
    }
    
    @GetMapping
    @Operation(summary = "Get all report accesses", 
               description = "Get all report accesses for an operator")
    public ResponseEntity<Map<String, List<ReportAccessResponse>>> getReportAccesses(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/report-access", operatorId);
        
        List<ReportAccessResponse> accesses = reportAccessService.getReportAccessesByOperator(operatorId);
        return ResponseEntity.ok(Map.of("reportAccesses", accesses));
    }
    
    @PutMapping("/{userIdentifier}")
    @Operation(summary = "Update report access", 
               description = "Update report access for a user")
    public ResponseEntity<ReportAccessResponse> updateReportAccess(
            @PathVariable Long operatorId,
            @PathVariable String userIdentifier,
            @Valid @RequestBody UpdateReportAccessRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/report-access/{}", operatorId, userIdentifier);
        
        ReportAccessResponse updated = reportAccessService.updateReportAccess(operatorId, userIdentifier, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{userIdentifier}")
    @Operation(summary = "Delete report access", 
               description = "Delete report access for a user")
    public ResponseEntity<Void> deleteReportAccess(
            @PathVariable Long operatorId,
            @PathVariable String userIdentifier) {
        log.debug("DELETE /api/v1/admin/operators/{}/report-access/{}", operatorId, userIdentifier);
        
        reportAccessService.deleteReportAccess(operatorId, userIdentifier);
        return ResponseEntity.noContent().build();
    }
}
