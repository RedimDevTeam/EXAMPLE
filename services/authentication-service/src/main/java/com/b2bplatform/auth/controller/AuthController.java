package com.b2bplatform.auth.controller;

import com.b2bplatform.auth.dto.request.CasinoLoginRequest;
import com.b2bplatform.auth.dto.request.LoginRequest;
import com.b2bplatform.auth.dto.request.RefreshTokenRequest;
import com.b2bplatform.auth.service.AuthenticationService;
import com.b2bplatform.common.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Player authentication APIs")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Internal/testing login: operatorCode + username + password. Use for testing only.
     */
    @PostMapping("/login")
    @Operation(summary = "Player login", description = "Authenticate player and get JWT tokens. Auto-creates player if doesn't exist.")
    public ResponseEntity<APIResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("POST /api/v1/auth/login - operator: {}, username: {}", request.getOperatorCode(), request.getUsername());
        APIResponse response = authenticationService.login(
            request.getOperatorCode(),
            request.getUsername(),
            request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Casino launch: X-Api-Key + player info (no password). Validates operator, find/create player, create session (DB + Redis), return launch URL.
     */
    @PostMapping("/launch")
    @Operation(summary = "Get game launch URL (casino flow)", description = "Validate operator via X-Api-Key, find or create player, create session in DB and Redis, return launch URL and session token.")
    public ResponseEntity<APIResponse> getLaunchUrl(
            @Valid @RequestBody CasinoLoginRequest loginRequest,
            HttpServletRequest request) {
        log.info("POST /api/v1/auth/launch - username: {}", loginRequest.getUsername());
        String apiKey = request.getHeader("X-Api-Key");
        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        APIResponse response = authenticationService.getLaunchUrl(loginRequest, apiKey, clientIp, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<APIResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("POST /api/v1/auth/refresh");
        APIResponse response = authenticationService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Validate token and return user info")
    public ResponseEntity<APIResponse> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        log.debug("GET /api/v1/auth/me");
        String token = authorization != null ? authorization.replace("Bearer ", "") : "";
        APIResponse response = authenticationService.validateToken(token);
        return ResponseEntity.ok(response);
    }
}
