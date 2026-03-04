package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateOperatorGameConfigRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorGameConfigRequest;
import com.b2bplatform.operator.dto.response.OperatorGameConfigResponse;
import com.b2bplatform.operator.service.OperatorGameConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing operator game configurations.
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/game-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator Game Configuration", description = "APIs for managing game configurations per operator (Gaming Provider Global Admins only)")
public class OperatorGameConfigController {
    
    private final OperatorGameConfigService operatorGameConfigService;
    
    @PostMapping
    @Operation(summary = "Create game configuration", 
               description = "Create a new game configuration for an operator")
    public ResponseEntity<OperatorGameConfigResponse> createGameConfig(
            @PathVariable Long operatorId,
            @Valid @RequestBody CreateOperatorGameConfigRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/game-config", operatorId);
        
        OperatorGameConfigResponse created = operatorGameConfigService.createGameConfig(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Get game configurations", 
               description = "Get game configurations for an operator, optionally filtered by provider and enabled status")
    public ResponseEntity<Map<String, List<OperatorGameConfigResponse>>> getGameConfigs(
            @PathVariable Long operatorId,
            @RequestParam(required = false) String gameProviderId,
            @RequestParam(defaultValue = "false") Boolean enabledOnly,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        log.debug("GET /api/v1/admin/operators/{}/game-config?gameProviderId={}&enabledOnly={}&activeOnly={}", 
            operatorId, gameProviderId, enabledOnly, activeOnly);
        
        List<OperatorGameConfigResponse> configs = operatorGameConfigService.getGameConfigs(
            operatorId, gameProviderId, enabledOnly, activeOnly);
        return ResponseEntity.ok(Map.of("configs", configs));
    }
    
    @GetMapping("/enabled")
    @Operation(summary = "Get enabled games", 
               description = "Get all enabled games for an operator (for game listing)")
    public ResponseEntity<Map<String, List<OperatorGameConfigResponse>>> getEnabledGames(
            @PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/game-config/enabled", operatorId);
        
        List<OperatorGameConfigResponse> games = operatorGameConfigService.getEnabledGames(operatorId);
        return ResponseEntity.ok(Map.of("games", games));
    }
    
    @GetMapping("/{gameProviderId}/{gameId}")
    @Operation(summary = "Get specific game configuration", 
               description = "Get configuration for a specific game")
    public ResponseEntity<OperatorGameConfigResponse> getGameConfig(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @PathVariable String gameId) {
        log.debug("GET /api/v1/admin/operators/{}/game-config/{}/{}", operatorId, gameProviderId, gameId);
        
        OperatorGameConfigResponse config = operatorGameConfigService.getGameConfig(
            operatorId, gameProviderId, gameId);
        return ResponseEntity.ok(config);
    }
    
    @PutMapping("/{gameProviderId}/{gameId}")
    @Operation(summary = "Update game configuration", 
               description = "Update game configuration settings")
    public ResponseEntity<OperatorGameConfigResponse> updateGameConfig(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @PathVariable String gameId,
            @Valid @RequestBody UpdateOperatorGameConfigRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/game-config/{}/{}", operatorId, gameProviderId, gameId);
        
        OperatorGameConfigResponse updated = operatorGameConfigService.updateGameConfig(
            operatorId, gameProviderId, gameId, request);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{gameProviderId}/{gameId}/enable")
    @Operation(summary = "Enable game", 
               description = "Enable a game for an operator")
    public ResponseEntity<OperatorGameConfigResponse> enableGame(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @PathVariable String gameId) {
        log.debug("PUT /api/v1/admin/operators/{}/game-config/{}/{}/enable", operatorId, gameProviderId, gameId);
        
        OperatorGameConfigResponse updated = operatorGameConfigService.setGameEnabled(
            operatorId, gameProviderId, gameId, true);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{gameProviderId}/{gameId}/disable")
    @Operation(summary = "Disable game", 
               description = "Disable a game for an operator")
    public ResponseEntity<OperatorGameConfigResponse> disableGame(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @PathVariable String gameId) {
        log.debug("PUT /api/v1/admin/operators/{}/game-config/{}/{}/disable", operatorId, gameProviderId, gameId);
        
        OperatorGameConfigResponse updated = operatorGameConfigService.setGameEnabled(
            operatorId, gameProviderId, gameId, false);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{gameProviderId}/{gameId}")
    @Operation(summary = "Deactivate game configuration", 
               description = "Deactivate a game configuration, optionally with an effective end date")
    public ResponseEntity<OperatorGameConfigResponse> deactivateGameConfig(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @PathVariable String gameId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime effectiveTo) {
        log.debug("DELETE /api/v1/admin/operators/{}/game-config/{}/{}?effectiveTo={}", 
            operatorId, gameProviderId, gameId, effectiveTo);
        
        OperatorGameConfigResponse deactivated = operatorGameConfigService.deactivateGameConfig(
            operatorId, gameProviderId, gameId, effectiveTo);
        return ResponseEntity.ok(deactivated);
    }
}
