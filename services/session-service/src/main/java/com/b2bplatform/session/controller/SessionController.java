package com.b2bplatform.session.controller;

import com.b2bplatform.common.enums.StatusCode;
import com.b2bplatform.common.response.APIResponse;
import com.b2bplatform.session.dto.request.CreateSessionRequest;
import com.b2bplatform.session.dto.response.SessionResponse;
import com.b2bplatform.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session Management", description = "Player session management APIs")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Create session", description = "Create a new session for a player")
    public ResponseEntity<APIResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        log.debug("POST /api/v1/sessions - player: {}, operator: {}", request.getPlayerId(), request.getOperatorId());
        SessionResponse response = sessionService.createSession(request);
        return ResponseEntity.ok(APIResponse.success(response));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Validate session", description = "Validate and get session details")
    public ResponseEntity<APIResponse> validateSession(@PathVariable String sessionId) {
        log.debug("GET /api/v1/sessions/{}", sessionId);
        Optional<SessionResponse> sessionOpt = sessionService.validateSession(sessionId);
        if (sessionOpt.isPresent()) {
            return ResponseEntity.ok(APIResponse.success(sessionOpt.get()));
        }
        return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_SESSION));
    }

    @PutMapping("/{sessionId}/refresh")
    @Operation(summary = "Refresh session", description = "Extend session expiration time")
    public ResponseEntity<APIResponse> refreshSession(@PathVariable String sessionId) {
        log.debug("PUT /api/v1/sessions/{}/refresh", sessionId);
        Optional<SessionResponse> sessionOpt = sessionService.refreshSession(sessionId);
        if (sessionOpt.isPresent()) {
            return ResponseEntity.ok(APIResponse.success(sessionOpt.get()));
        }
        return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_SESSION));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "End session", description = "End a session (logout)")
    public ResponseEntity<APIResponse> endSession(@PathVariable String sessionId) {
        log.debug("DELETE /api/v1/sessions/{}", sessionId);
        boolean ended = sessionService.endSession(sessionId);
        if (ended) {
            return ResponseEntity.ok(APIResponse.success(null));
        }
        return ResponseEntity.ok(APIResponse.get(StatusCode.INVALID_SESSION));
    }

    @GetMapping("/player/{playerId}")
    @Operation(summary = "Get player sessions", description = "Get all active sessions for a player")
    public ResponseEntity<APIResponse> getPlayerSessions(@PathVariable Long playerId) {
        log.debug("GET /api/v1/sessions/player/{}", playerId);
        List<SessionResponse> sessions = sessionService.getPlayerSessions(playerId);
        return ResponseEntity.ok(APIResponse.success(sessions));
    }
}
