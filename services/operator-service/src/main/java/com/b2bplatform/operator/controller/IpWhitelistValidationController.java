package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.response.ErrorResponse;
import com.b2bplatform.operator.service.OperatorIpWhitelistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for IP whitelist validation.
 * Used by API Gateway and other services to validate IP addresses.
 */
@RestController
@RequestMapping("/api/v1/internal/ip-whitelist")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "IP Whitelist Validation", description = "Internal APIs for IP whitelist validation")
public class IpWhitelistValidationController {
    
    private final OperatorIpWhitelistService ipWhitelistService;
    
    @GetMapping("/validate")
    @Operation(summary = "Validate IP address", description = "Check if an IP address is whitelisted for an operator and endpoint (internal use)")
    public ResponseEntity<Boolean> validateIp(
            @RequestParam Long operatorId,
            @RequestParam String ipAddress,
            @RequestParam(required = false, defaultValue = "/api/v1/") String endpoint) {
        log.debug("Internal IP validation - Operator: {}, IP: {}, Endpoint: {}", operatorId, ipAddress, endpoint);
        
        boolean isWhitelisted = ipWhitelistService.isIpWhitelisted(operatorId, ipAddress, endpoint);
        return ResponseEntity.ok(isWhitelisted);
    }
}
