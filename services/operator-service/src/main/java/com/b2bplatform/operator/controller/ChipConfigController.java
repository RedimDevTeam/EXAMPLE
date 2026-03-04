package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.*;
import com.b2bplatform.operator.dto.response.BetLimitTypeResponse;
import com.b2bplatform.operator.dto.response.ChipDenominationResponse;
import com.b2bplatform.operator.dto.response.ChipDenominationsResponse;
import com.b2bplatform.operator.model.BetLimitType;
import com.b2bplatform.operator.service.ChipConfigService;
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
 * Controller for managing chip denominations and bet limit types.
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/games/{gameId}/chip-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chip Configuration", description = "APIs for managing chip denominations and bet limit types (Gaming Provider Global Admins only)")
public class ChipConfigController {
    
    private final ChipConfigService chipConfigService;
    
    // ==================== Chip Denominations ====================
    
    @PostMapping("/chips")
    @Operation(summary = "Create chip denomination", 
               description = "Create a single chip denomination. Supports flexible chip counts (e.g., 5, 6, 7 chips) based on UI space. Chip index starts at 0.")
    public ResponseEntity<ChipDenominationResponse> createChipDenomination(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @Valid @RequestBody CreateChipDenominationRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/games/{}/chip-config/chips", operatorId, gameId);
        
        ChipDenominationResponse created = chipConfigService.createChipDenomination(operatorId, gameId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PostMapping("/chips/bulk")
    @Operation(summary = "Bulk create chip denominations", 
               description = "Create multiple chip denominations at once (e.g., from Excel upload). Supports flexible chip counts (e.g., 5, 6, 7 chips) based on UI space availability. Chip indices start at 0.")
    public ResponseEntity<Map<String, List<ChipDenominationResponse>>> bulkCreateChipDenominations(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @Valid @RequestBody BulkCreateChipDenominationsRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/games/{}/chip-config/chips/bulk", operatorId, gameId);
        
        List<ChipDenominationResponse> created = chipConfigService.bulkCreateChipDenominations(operatorId, gameId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("chips", created));
    }
    
    @GetMapping("/chips")
    @Operation(summary = "Get chip denominations", 
               description = "Get chip denominations for an operator, game, and currency")
    public ResponseEntity<ChipDenominationsResponse> getChipDenominations(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @RequestParam String currencyCode,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        log.debug("GET /api/v1/admin/operators/{}/games/{}/chip-config/chips?currencyCode={}&activeOnly={}", 
            operatorId, gameId, currencyCode, activeOnly);
        
        ChipDenominationsResponse response = chipConfigService.getChipDenominations(
            operatorId, gameId, currencyCode, activeOnly);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/chips/{chipIndex}")
    @Operation(summary = "Update chip denomination", 
               description = "Update a chip denomination by index")
    public ResponseEntity<ChipDenominationResponse> updateChipDenomination(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @RequestParam String currencyCode,
            @PathVariable Integer chipIndex,
            @Valid @RequestBody UpdateChipDenominationRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/games/{}/chip-config/chips/{}?currencyCode={}", 
            operatorId, gameId, chipIndex, currencyCode);
        
        ChipDenominationResponse updated = chipConfigService.updateChipDenomination(
            operatorId, gameId, currencyCode, chipIndex, request);
        return ResponseEntity.ok(updated);
    }
    
    // ==================== Bet Limit Types ====================
    
    @PostMapping("/bet-limit-types")
    @Operation(summary = "Create bet limit type", 
               description = "Create a bet limit type (Standard, VIP, Promotional, or Custom)")
    public ResponseEntity<BetLimitTypeResponse> createBetLimitType(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @Valid @RequestBody CreateBetLimitTypeRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/games/{}/chip-config/bet-limit-types", operatorId, gameId);
        
        BetLimitTypeResponse created = chipConfigService.createBetLimitType(operatorId, gameId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/bet-limit-types")
    @Operation(summary = "Get bet limit types", 
               description = "Get bet limit types for an operator, game, and currency")
    public ResponseEntity<Map<String, List<BetLimitTypeResponse>>> getBetLimitTypes(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @RequestParam String currencyCode,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        log.debug("GET /api/v1/admin/operators/{}/games/{}/chip-config/bet-limit-types?currencyCode={}&activeOnly={}", 
            operatorId, gameId, currencyCode, activeOnly);
        
        List<BetLimitTypeResponse> limitTypes = chipConfigService.getBetLimitTypes(
            operatorId, gameId, currencyCode, activeOnly);
        return ResponseEntity.ok(Map.of("betLimitTypes", limitTypes));
    }
    
    @PutMapping("/bet-limit-types/{limitType}")
    @Operation(summary = "Update bet limit type", 
               description = "Update a bet limit type")
    public ResponseEntity<BetLimitTypeResponse> updateBetLimitType(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @RequestParam String currencyCode,
            @PathVariable BetLimitType limitType,
            @Valid @RequestBody UpdateBetLimitTypeRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/games/{}/chip-config/bet-limit-types/{}?currencyCode={}", 
            operatorId, gameId, limitType, currencyCode);
        
        BetLimitTypeResponse updated = chipConfigService.updateBetLimitType(
            operatorId, gameId, currencyCode, limitType, request);
        return ResponseEntity.ok(updated);
    }
}
