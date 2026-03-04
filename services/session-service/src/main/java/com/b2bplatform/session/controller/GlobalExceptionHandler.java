package com.b2bplatform.session.controller;

import com.b2bplatform.common.enums.StatusCode;
import com.b2bplatform.common.response.APIResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String errors = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        log.warn("Constraint violation: {}", errors);
        return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_REQUEST, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.ok(APIResponse.get(StatusCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }
}
