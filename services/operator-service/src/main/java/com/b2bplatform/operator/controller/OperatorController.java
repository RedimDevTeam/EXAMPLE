package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.request.CreateOperatorRequest;
import com.b2bplatform.operator.dto.request.UpdateOperatorRequest;
import com.b2bplatform.operator.dto.response.OperatorResponse;
import com.b2bplatform.operator.service.OperatorService;
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

@RestController
@RequestMapping("/api/v1/operators")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operator Management", description = "APIs for managing operators")
public class OperatorController {
    
    private final OperatorService operatorService;
    
    @GetMapping
    @Operation(summary = "Get all operators", description = "Retrieve a list of all operators")
    public ResponseEntity<List<OperatorResponse>> getAllOperators() {
        log.debug("GET /api/v1/operators - Fetching all operators");
        // Exception handling is done by GlobalExceptionHandler
        return ResponseEntity.ok(operatorService.getAllOperators());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get operator by ID", description = "Retrieve an operator by its ID")
    public ResponseEntity<OperatorResponse> getOperatorById(@PathVariable Long id) {
        log.debug("GET /api/v1/operators/{} - Fetching operator", id);
        // Exception handling is done by GlobalExceptionHandler
        Optional<OperatorResponse> operator = operatorService.getOperatorById(id);
        return operator.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{code}")
    @Operation(summary = "Get operator by code", description = "Retrieve an operator by its code")
    public ResponseEntity<OperatorResponse> getOperatorByCode(@PathVariable String code) {
        log.debug("GET /api/v1/operators/code/{} - Fetching operator", code);
        // Exception handling is done by GlobalExceptionHandler
        Optional<OperatorResponse> operator = operatorService.getOperatorByCode(code);
        return operator.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create operator", description = "Create a new operator")
    public ResponseEntity<OperatorResponse> createOperator(@Valid @RequestBody CreateOperatorRequest request) {
        log.debug("POST /api/v1/operators - Creating operator with code: {}", request.getCode());
        // Exception handling is done by GlobalExceptionHandler
        OperatorResponse created = operatorService.createOperator(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update operator", description = "Update an existing operator")
    public ResponseEntity<OperatorResponse> updateOperator(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOperatorRequest request) {
        log.debug("PUT /api/v1/operators/{} - Updating operator", id);
        // Exception handling is done by GlobalExceptionHandler
        OperatorResponse updated = operatorService.updateOperator(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete operator", description = "Delete an operator by ID")
    public ResponseEntity<Void> deleteOperator(@PathVariable Long id) {
        log.debug("DELETE /api/v1/operators/{} - Deleting operator", id);
        // Exception handling is done by GlobalExceptionHandler
        operatorService.deleteOperator(id);
        return ResponseEntity.noContent().build();
    }
}
