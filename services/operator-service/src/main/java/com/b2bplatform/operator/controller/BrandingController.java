package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.UpdateBrandingRequest;
import com.b2bplatform.operator.dto.response.BrandingResponse;
import com.b2bplatform.operator.model.LogoType;
import com.b2bplatform.operator.service.BrandingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing branding assets (logos).
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/branding")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Branding Configuration", description = "APIs for managing operator branding assets (Gaming Provider Global Admins only)")
public class BrandingController {
    
    private final BrandingService brandingService;
    
    // ==================== Operator Branding ====================
    
    @PostMapping("/operator-logos")
    @Operation(summary = "Upload operator logo", 
               description = "Upload operator logo (PNG, JPG, SVG). Supports OPERATOR_LOGO, FAVICON, MOBILE_LOGO, DESKTOP_LOGO types.")
    public ResponseEntity<BrandingResponse> uploadOperatorLogo(
            @PathVariable Long operatorId,
            @RequestParam LogoType logoType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "0") Integer displayOrder) throws IOException {
        log.debug("POST /api/v1/admin/operators/{}/branding/operator-logos", operatorId);
        
        BrandingResponse uploaded = brandingService.uploadOperatorLogo(operatorId, logoType, file, displayOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }
    
    @GetMapping("/operator-logos")
    @Operation(summary = "Get operator logos", 
               description = "Get operator branding assets. Optionally filter by logo type.")
    public ResponseEntity<Map<String, List<BrandingResponse>>> getOperatorBranding(
            @PathVariable Long operatorId,
            @RequestParam(required = false) LogoType logoType) {
        log.debug("GET /api/v1/admin/operators/{}/branding/operator-logos?logoType={}", operatorId, logoType);
        
        List<BrandingResponse> branding = brandingService.getOperatorBranding(operatorId, logoType);
        return ResponseEntity.ok(Map.of("branding", branding));
    }
    
    @PutMapping("/operator-logos/{logoType}/{displayOrder}")
    @Operation(summary = "Update operator branding", 
               description = "Update operator branding metadata (active status, display order)")
    public ResponseEntity<BrandingResponse> updateOperatorBranding(
            @PathVariable Long operatorId,
            @PathVariable LogoType logoType,
            @PathVariable Integer displayOrder,
            @Valid @RequestBody UpdateBrandingRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/branding/operator-logos/{}/{}", operatorId, logoType, displayOrder);
        
        BrandingResponse updated = brandingService.updateOperatorBranding(operatorId, logoType, displayOrder, request);
        return ResponseEntity.ok(updated);
    }
    
    // ==================== Game Provider Branding ====================
    
    @PostMapping("/game-providers/{gameProviderId}/logos")
    @Operation(summary = "Upload game provider logo", 
               description = "Upload game provider logo (PNG, JPG, SVG). Supports PROVIDER_LOGO, ICON, BANNER types.")
    public ResponseEntity<BrandingResponse> uploadGameProviderLogo(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @RequestParam LogoType logoType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "0") Integer displayOrder) throws IOException {
        log.debug("POST /api/v1/admin/operators/{}/branding/game-providers/{}/logos", operatorId, gameProviderId);
        
        BrandingResponse uploaded = brandingService.uploadGameProviderLogo(
            operatorId, gameProviderId, logoType, file, displayOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }
    
    @GetMapping("/game-providers/{gameProviderId}/logos")
    @Operation(summary = "Get game provider logos", 
               description = "Get game provider branding assets. Optionally filter by logo type.")
    public ResponseEntity<Map<String, List<BrandingResponse>>> getGameProviderBranding(
            @PathVariable Long operatorId,
            @PathVariable String gameProviderId,
            @RequestParam(required = false) LogoType logoType) {
        log.debug("GET /api/v1/admin/operators/{}/branding/game-providers/{}/logos?logoType={}", 
            operatorId, gameProviderId, logoType);
        
        List<BrandingResponse> branding = brandingService.getGameProviderBranding(operatorId, gameProviderId, logoType);
        return ResponseEntity.ok(Map.of("branding", branding));
    }
    
    // ==================== Game Branding ====================
    
    @PostMapping("/games/{gameId}/logos")
    @Operation(summary = "Upload game logo", 
               description = "Upload game logo (PNG, JPG, SVG). Supports GAME_LOGO, THUMBNAIL, BANNER, ICON types.")
    public ResponseEntity<BrandingResponse> uploadGameLogo(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @RequestParam String gameProviderId,
            @RequestParam LogoType logoType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "0") Integer displayOrder) throws IOException {
        log.debug("POST /api/v1/admin/operators/{}/branding/games/{}/logos", operatorId, gameId);
        
        BrandingResponse uploaded = brandingService.uploadGameLogo(
            operatorId, gameId, gameProviderId, logoType, file, displayOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }
    
    @GetMapping("/games/{gameId}/logos")
    @Operation(summary = "Get game logos", 
               description = "Get game branding assets. Optionally filter by logo type.")
    public ResponseEntity<Map<String, List<BrandingResponse>>> getGameBranding(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @RequestParam(required = false) LogoType logoType) {
        log.debug("GET /api/v1/admin/operators/{}/branding/games/{}/logos?logoType={}", operatorId, gameId, logoType);
        
        List<BrandingResponse> branding = brandingService.getGameBranding(operatorId, gameId, logoType);
        return ResponseEntity.ok(Map.of("branding", branding));
    }
}
