package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.AddOperatorLanguageRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorLanguageRequest;
import com.b2bplatform.operator.dto.response.OperatorLanguageResponse;
import com.b2bplatform.operator.service.OperatorLanguageService;
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
 * Controller for managing operator languages (multi-language support).
 * Access: Gaming Provider Global Admins only.
 */
@RestController
@RequestMapping("/api/v1/admin/operators/{operatorId}/languages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator Languages", description = "APIs for managing operator languages (Gaming Provider Global Admins only)")
public class OperatorLanguageController {
    
    private final OperatorLanguageService operatorLanguageService;
    
    @PostMapping
    @Operation(summary = "Add language to operator", 
               description = "Add a new supported language to an operator")
    public ResponseEntity<OperatorLanguageResponse> addLanguage(
            @PathVariable Long operatorId,
            @Valid @RequestBody AddOperatorLanguageRequest request) {
        log.debug("POST /api/v1/admin/operators/{}/languages", operatorId);
        
        OperatorLanguageResponse added = operatorLanguageService.addLanguage(operatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }
    
    @GetMapping
    @Operation(summary = "Get operator languages", 
               description = "Get all languages supported by an operator")
    public ResponseEntity<Map<String, List<OperatorLanguageResponse>>> getLanguages(
            @PathVariable Long operatorId,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        log.debug("GET /api/v1/admin/operators/{}/languages?activeOnly={}", operatorId, activeOnly);
        
        List<OperatorLanguageResponse> languages = operatorLanguageService.getLanguages(operatorId, activeOnly);
        return ResponseEntity.ok(Map.of("languages", languages));
    }
    
    @GetMapping("/{languageCode}")
    @Operation(summary = "Get specific language", 
               description = "Get details of a specific language for an operator")
    public ResponseEntity<OperatorLanguageResponse> getLanguage(
            @PathVariable Long operatorId,
            @PathVariable String languageCode) {
        log.debug("GET /api/v1/admin/operators/{}/languages/{}", operatorId, languageCode);
        
        OperatorLanguageResponse language = operatorLanguageService.getLanguage(operatorId, languageCode);
        return ResponseEntity.ok(language);
    }
    
    @GetMapping("/default")
    @Operation(summary = "Get default language", 
               description = "Get the default language for an operator")
    public ResponseEntity<OperatorLanguageResponse> getDefaultLanguage(
            @PathVariable Long operatorId) {
        log.debug("GET /api/v1/admin/operators/{}/languages/default", operatorId);
        
        OperatorLanguageResponse language = operatorLanguageService.getDefaultLanguage(operatorId);
        return ResponseEntity.ok(language);
    }
    
    @PutMapping("/{languageCode}")
    @Operation(summary = "Update language", 
               description = "Update language settings for an operator")
    public ResponseEntity<OperatorLanguageResponse> updateLanguage(
            @PathVariable Long operatorId,
            @PathVariable String languageCode,
            @Valid @RequestBody UpdateOperatorLanguageRequest request) {
        log.debug("PUT /api/v1/admin/operators/{}/languages/{}", operatorId, languageCode);
        
        OperatorLanguageResponse updated = operatorLanguageService.updateLanguage(operatorId, languageCode, request);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{languageCode}/set-default")
    @Operation(summary = "Set default language", 
               description = "Set a language as the default for an operator")
    public ResponseEntity<OperatorLanguageResponse> setDefaultLanguage(
            @PathVariable Long operatorId,
            @PathVariable String languageCode) {
        log.debug("PUT /api/v1/admin/operators/{}/languages/{}/set-default", operatorId, languageCode);
        
        OperatorLanguageResponse updated = operatorLanguageService.setDefaultLanguage(operatorId, languageCode);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{languageCode}")
    @Operation(summary = "Remove language", 
               description = "Remove a language from an operator")
    public ResponseEntity<Void> removeLanguage(
            @PathVariable Long operatorId,
            @PathVariable String languageCode) {
        log.debug("DELETE /api/v1/admin/operators/{}/languages/{}", operatorId, languageCode);
        
        operatorLanguageService.removeLanguage(operatorId, languageCode);
        return ResponseEntity.noContent().build();
    }
}
