package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.AddOperatorCurrencyRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorCurrencyRequest;
import com.b2bplatform.operator.dto.response.OperatorCurrencyResponse;
import com.b2bplatform.operator.service.OperatorCurrencyService;
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
 * Controller for managing operator currencies (multi-currency support).
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/currencies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator Currencies", description = "APIs for managing operator currencies (Gaming Provider Global Admins only)")
public class OperatorCurrencyController {
    
    private final OperatorCurrencyService operatorCurrencyService;
    
    @PostMapping
    @Operation(summary = "Add currency to operator", 
               description = "Add a new supported currency to an operator")
    public ResponseEntity<OperatorCurrencyResponse> addCurrency(
            @PathVariable Long operatorId,
            @Valid @RequestBody AddOperatorCurrencyRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/currencies", operatorId);
        
        OperatorCurrencyResponse added = operatorCurrencyService.addCurrency(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }
    
    @GetMapping
    @Operation(summary = "Get operator currencies", 
               description = "Get all currencies supported by an operator")
    public ResponseEntity<Map<String, List<OperatorCurrencyResponse>>> getCurrencies(
            @PathVariable Long operatorId,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        log.debug("GET /api/v1/admin/operators/{}/currencies?activeOnly={}", operatorId, activeOnly);
        
        List<OperatorCurrencyResponse> currencies = operatorCurrencyService.getCurrencies(operatorId, activeOnly);
        return ResponseEntity.ok(Map.of("currencies", currencies));
    }
    
    @GetMapping("/{currencyCode}")
    @Operation(summary = "Get specific currency", 
               description = "Get details of a specific currency for an operator")
    public ResponseEntity<OperatorCurrencyResponse> getCurrency(
            @PathVariable Long operatorId,
            @PathVariable String currencyCode) {
        log.debug("GET /api/v1/admin/operators/{}/currencies/{}", operatorId, currencyCode);
        
        OperatorCurrencyResponse currency = operatorCurrencyService.getCurrency(operatorId, currencyCode);
        return ResponseEntity.ok(currency);
    }
    
    @GetMapping("/default")
    @Operation(summary = "Get default currency", 
               description = "Get the default currency for an operator")
    public ResponseEntity<OperatorCurrencyResponse> getDefaultCurrency(
            @PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/currencies/default", operatorId);
        
        OperatorCurrencyResponse currency = operatorCurrencyService.getDefaultCurrency(operatorId);
        return ResponseEntity.ok(currency);
    }
    
    @PutMapping("/{currencyCode}")
    @Operation(summary = "Update currency", 
               description = "Update currency settings for an operator")
    public ResponseEntity<OperatorCurrencyResponse> updateCurrency(
            @PathVariable Long operatorId,
            @PathVariable String currencyCode,
            @Valid @RequestBody UpdateOperatorCurrencyRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/currencies/{}", operatorId, currencyCode);
        
        OperatorCurrencyResponse updated = operatorCurrencyService.updateCurrency(operatorId, currencyCode, request);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{currencyCode}/set-default")
    @Operation(summary = "Set default currency", 
               description = "Set a currency as the default for an operator")
    public ResponseEntity<OperatorCurrencyResponse> setDefaultCurrency(
            @PathVariable Long operatorId,
            @PathVariable String currencyCode) {
        log.debug("PUT /api/v1/admin/operators/{}/currencies/{}/set-default", operatorId, currencyCode);
        
        OperatorCurrencyResponse updated = operatorCurrencyService.setDefaultCurrency(operatorId, currencyCode);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{currencyCode}")
    @Operation(summary = "Remove currency", 
               description = "Remove a currency from an operator")
    public ResponseEntity<Void> removeCurrency(
            @PathVariable Long operatorId,
            @PathVariable String currencyCode) {
        log.debug("DELETE /api/v1/admin/operators/{}/currencies/{}", operatorId, currencyCode);
        
        operatorCurrencyService.removeCurrency(operatorId, currencyCode);
        return ResponseEntity.noContent().build();
    }
}
