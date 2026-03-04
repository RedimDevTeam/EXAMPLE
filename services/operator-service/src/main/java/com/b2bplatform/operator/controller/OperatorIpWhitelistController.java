package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateIpWhitelistRequest;
import com.b2bplatform.operator.dto.request.UpdateIpWhitelistRequest;
import com.b2bplatform.operator.dto.response.IpWhitelistResponse;
import com.b2bplatform.operator.service.OperatorIpWhitelistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing operator IP whitelist entries.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/ip-whitelist")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator IP Whitelist", description = "APIs for managing operator IP whitelist")
public class OperatorIpWhitelistController {
    
    private final OperatorIpWhitelistService ipWhitelistService;
    
    @GetMapping
    @Operation(summary = "Get all IP whitelist entries", description = "Retrieve all IP whitelist entries for an operator")
    public ResponseEntity<List<IpWhitelistResponse>> getAllIpWhitelist(
            @PathVariable Long operatorId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        log.debug("GET /api/v1/admin/operators/{}/ip-whitelist - activeOnly: {}", operatorId, activeOnly);
        
        List<IpWhitelistResponse> entries = activeOnly ? 
            ipWhitelistService.getActiveIpWhitelistByOperatorId(operatorId) :
            ipWhitelistService.getIpWhitelistByOperatorId(operatorId);
        
        return ResponseEntity.ok(entries);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get IP whitelist entry by ID", description = "Retrieve a specific IP whitelist entry")
    public ResponseEntity<IpWhitelistResponse> getIpWhitelistById(
            @PathVariable Long operatorId,
            @PathVariable Long id) {
        log.debug("GET /api/v1/admin/operators/{}/ip-whitelist/{}", operatorId, id);
        
        Optional<IpWhitelistResponse> entry = ipWhitelistService.getIpWhitelistById(id);
        return entry.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create IP whitelist entry", description = "Add a new IP address to the operator's whitelist")
    public ResponseEntity<IpWhitelistResponse> createIpWhitelist(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateIpWhitelistRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/ip-whitelist - Creating entry for IP: {}", operatorId, request.getIpAddress());
        
        IpWhitelistResponse created = ipWhitelistService.createIpWhitelist(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update IP whitelist entry", description = "Update an existing IP whitelist entry")
    public ResponseEntity<IpWhitelistResponse> updateIpWhitelist(
            @PathVariable Long operatorId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateIpWhitelistRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/ip-whitelist/{}", operatorId, id);
        
        IpWhitelistResponse updated = ipWhitelistService.updateIpWhitelist(operatorId, id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete IP whitelist entry", description = "Remove an IP address from the operator's whitelist")
    public ResponseEntity<Void> deleteIpWhitelist(
            @PathVariable Long operatorId,
            @PathVariable Long id) {
        log.debug("DELETE /api/v1/admin/operators/{}/ip-whitelist/{}", operatorId, id);
        
        ipWhitelistService.deleteIpWhitelist(operatorId, id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/validate")
    @Operation(summary = "Validate IP address", description = "Check if an IP address is whitelisted for an operator and endpoint")
    public ResponseEntity<Boolean> validateIp(
            @PathVariable Long operatorId,
            @RequestParam String ipAddress,
            @RequestParam(required = false, defaultValue = "/api/v1/") String endpoint) {
        log.debug("GET /api/v1/admin/operators/{}/ip-whitelist/validate - IP: {}, Endpoint: {}", operatorId, ipAddress, endpoint);
        
        boolean isWhitelisted = ipWhitelistService.isIpWhitelisted(operatorId, ipAddress, endpoint);
        return ResponseEntity.ok(isWhitelisted);
    }
}
