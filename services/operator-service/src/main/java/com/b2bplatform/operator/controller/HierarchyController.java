package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateCreditAllocationRequest;
import com.b2bplatform.operator.dto.request.CreateHierarchyRequest;
import com.b2bplatform.operator.dto.request.CreateRevenueSharingRequest;
import com.b2bplatform.operator.dto.request.UpdateCreditAllocationRequest;
import com.b2bplatform.operator.dto.request.UpdateRevenueSharingRequest;
import com.b2bplatform.operator.model.RevenueType;
import com.b2bplatform.operator.dto.response.CreditAllocationResponse;
import com.b2bplatform.operator.dto.response.HierarchyResponse;
import com.b2bplatform.operator.dto.response.RevenueSharingResponse;
import com.b2bplatform.operator.service.HierarchyService;
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
 * Controller for managing operator hierarchy, revenue sharing, and credit allocation.
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/hierarchy")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator Hierarchy", description = "APIs for managing operator hierarchy, revenue sharing, and credit allocation (Gaming Provider Global Admins only)")
public class HierarchyController {
    
    private final HierarchyService hierarchyService;
    
    @PostMapping
    @Operation(summary = "Create operator hierarchy", 
               description = "Create hierarchy entry for operator (Master level 1, Agent level 2, Sub-Agent level 3)")
    public ResponseEntity<HierarchyResponse> createHierarchy(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateHierarchyRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/hierarchy", operatorId);
        
        HierarchyResponse created = hierarchyService.createHierarchy(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Get operator hierarchy", 
               description = "Get hierarchy information for an operator")
    public ResponseEntity<HierarchyResponse> getHierarchy(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/hierarchy", operatorId);
        
        HierarchyResponse hierarchy = hierarchyService.getHierarchy(operatorId);
        return ResponseEntity.ok(hierarchy);
    }
    
    @PostMapping("/revenue-sharing")
    @Operation(summary = "Create revenue sharing", 
               description = "Create revenue sharing configuration between operator and parent")
    public ResponseEntity<RevenueSharingResponse> createRevenueSharing(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateRevenueSharingRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/hierarchy/revenue-sharing", operatorId);
        
        RevenueSharingResponse created = hierarchyService.createRevenueSharing(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PostMapping("/credit-allocation")
    @Operation(summary = "Create credit allocation", 
               description = "Allocate credit from parent operator to child operator")
    public ResponseEntity<CreditAllocationResponse> createCreditAllocation(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateCreditAllocationRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/hierarchy/credit-allocation", operatorId);
        
        CreditAllocationResponse created = hierarchyService.createCreditAllocation(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/revenue-sharing")
    @Operation(summary = "Get revenue sharing configurations", 
               description = "Get all revenue sharing configurations for an operator")
    public ResponseEntity<Map<String, List<RevenueSharingResponse>>> getRevenueSharings(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/hierarchy/revenue-sharing", operatorId);
        
        List<RevenueSharingResponse> sharings = hierarchyService.getRevenueSharings(operatorId);
        return ResponseEntity.ok(Map.of("revenueSharings", sharings));
    }
    
    @PutMapping("/revenue-sharing/{parentOperatorId}/{revenueType}")
    @Operation(summary = "Update revenue sharing", 
               description = "Update revenue sharing configuration")
    public ResponseEntity<RevenueSharingResponse> updateRevenueSharing(
            @PathVariable Long operatorId,
            @PathVariable Long parentOperatorId,
            @PathVariable RevenueType revenueType,
            @Valid @RequestBody UpdateRevenueSharingRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/hierarchy/revenue-sharing/{}/{}", 
            operatorId, parentOperatorId, revenueType);
        
        RevenueSharingResponse updated = hierarchyService.updateRevenueSharing(
            operatorId, parentOperatorId, revenueType, request);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/credit-allocation")
    @Operation(summary = "Get credit allocations", 
               description = "Get all credit allocations from this operator to child operators")
    public ResponseEntity<Map<String, List<CreditAllocationResponse>>> getCreditAllocations(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/hierarchy/credit-allocation", operatorId);
        
        List<CreditAllocationResponse> allocations = hierarchyService.getCreditAllocations(operatorId);
        return ResponseEntity.ok(Map.of("creditAllocations", allocations));
    }
    
    @PutMapping("/credit-allocation/{childOperatorId}/{currencyCode}")
    @Operation(summary = "Update credit allocation", 
               description = "Update credit allocation from parent to child operator")
    public ResponseEntity<CreditAllocationResponse> updateCreditAllocation(
            @PathVariable Long operatorId,
            @PathVariable Long childOperatorId,
            @PathVariable String currencyCode,
            @Valid @RequestBody UpdateCreditAllocationRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/hierarchy/credit-allocation/{}/{}", 
            operatorId, childOperatorId, currencyCode);
        
        CreditAllocationResponse updated = hierarchyService.updateCreditAllocation(
            operatorId, childOperatorId, currencyCode, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/credit-allocation/{childOperatorId}/{currencyCode}")
    @Operation(summary = "Delete credit allocation", 
               description = "Delete credit allocation from parent to child operator")
    public ResponseEntity<Void> deleteCreditAllocation(
            @PathVariable Long operatorId,
            @PathVariable Long childOperatorId,
            @PathVariable String currencyCode) {
        log.debug("DELETE /api/v1/admin/operators/{}/hierarchy/credit-allocation/{}/{}", 
            operatorId, childOperatorId, currencyCode);
        
        hierarchyService.deleteCreditAllocation(operatorId, childOperatorId, currencyCode);
        return ResponseEntity.noContent().build();
    }
}
