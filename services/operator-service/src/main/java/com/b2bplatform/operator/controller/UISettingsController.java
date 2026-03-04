package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.UpdateUISettingsRequest;
import com.b2bplatform.operator.dto.response.UISettingsResponse;
import com.b2bplatform.operator.service.UISettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing operator UI settings.
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/ui-settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "UI Settings", description = "APIs for managing operator UI configuration settings (Gaming Provider Global Admins only)")
public class UISettingsController {
    
    private final UISettingsService uiSettingsService;
    
    @GetMapping
    @Operation(summary = "Get UI settings", 
               description = "Get operator UI settings. Returns default settings if none exist.")
    public ResponseEntity<UISettingsResponse> getUISettings(@PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/ui-settings", operatorId);
        
        UISettingsResponse settings = uiSettingsService.getUISettings(operatorId);
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping
    @Operation(summary = "Update UI settings", 
               description = "Update operator UI settings. Only provided fields will be updated.")
    public ResponseEntity<UISettingsResponse> updateUISettings(
            @PathVariable Long operatorId,
            @Valid @RequestBody UpdateUISettingsRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/ui-settings", operatorId);
        
        UISettingsResponse updated = uiSettingsService.updateUISettings(operatorId, request);
        return ResponseEntity.ok(updated);
    }
}
