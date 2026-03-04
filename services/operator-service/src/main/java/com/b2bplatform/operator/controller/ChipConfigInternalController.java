package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.response.ChipDenominationsResponse;
import com.b2bplatform.operator.service.ChipConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for chip denominations (used by Bet Service).
 * Provides read-only access to chip denominations.
 */
@RestController
@RequestMapping("/api/v1/internal/chip-denominations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chip Denominations (Internal)", description = "Internal APIs for retrieving chip denominations (used by Bet Service)")
public class ChipConfigInternalController {
    
    private final ChipConfigService chipConfigService;
    
    @GetMapping("/{operatorId}/{gameId}/{currencyCode}")
    @Operation(summary = "Get chip denominations", 
               description = "Get active chip denominations for an operator, game, and currency. Used by Bet Service for chip selection.")
    public ResponseEntity<ChipDenominationsResponse> getChipDenominations(
            @PathVariable Long operatorId,
            @PathVariable String gameId,
            @PathVariable String currencyCode) {
        log.debug("GET /api/v1/internal/chip-denominations/{}/{}/{}", operatorId, gameId, currencyCode);
        
        ChipDenominationsResponse response = chipConfigService.getChipDenominations(
            operatorId, gameId, currencyCode, true); // Only active chips
        return ResponseEntity.ok(response);
    }
}
