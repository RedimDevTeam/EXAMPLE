package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        log.warn("Validation failed: {}", errors);
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message(errors)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String errors = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("Constraint violation: {}", errors);
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message(errors)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
        // Ignore favicon.ico and other static resource requests
        if (ex.getResourcePath() != null && ex.getResourcePath().contains("favicon.ico")) {
            // Silently ignore favicon requests
            return ResponseEntity.notFound().build();
        }
        log.debug("Resource not found: {}", ex.getResourcePath());
        return ResponseEntity.notFound().build();
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        // Don't log NoResourceFoundException for favicon as error
        if (ex instanceof NoResourceFoundException) {
            NoResourceFoundException nrfe = (NoResourceFoundException) ex;
            if (nrfe.getResourcePath() != null && nrfe.getResourcePath().contains("favicon.ico")) {
                return ResponseEntity.notFound().build();
            }
        }
        
        // Log the full exception with stack trace for debugging
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        // Check if it's a database-related error (likely migration not run)
        String errorMessage = "An unexpected error occurred";
        if (ex.getMessage() != null) {
            String lowerMessage = ex.getMessage().toLowerCase();
            if (lowerMessage.contains("column") && lowerMessage.contains("does not exist")) {
                errorMessage = "Database schema error: Please run migration script 004_add_operator_maintenance_mode.sql";
            } else if (lowerMessage.contains("relation") && lowerMessage.contains("does not exist")) {
                errorMessage = "Database schema error: Please run migration scripts";
            } else {
                // Include the actual error message for debugging (in development)
                errorMessage = ex.getMessage();
            }
        }
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message(errorMessage)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
