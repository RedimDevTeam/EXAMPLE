package com.b2bplatform.auth.service;

import com.b2bplatform.auth.dto.request.CasinoLoginRequest;
import com.b2bplatform.auth.dto.response.GameLaunchUrlResponse;
import com.b2bplatform.auth.dto.response.LoginResponse;
import com.b2bplatform.auth.dto.response.RefreshTokenResponse;
import com.b2bplatform.auth.dto.response.UserInfoResponse;
import com.b2bplatform.auth.model.Player;
import com.b2bplatform.auth.repository.PlayerRepository;
import com.b2bplatform.common.enums.StatusCode;
import com.b2bplatform.common.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private static final String CASINO_NO_PASSWORD_PLACEHOLDER = "CASINO_NO_PASSWORD";

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final OperatorServiceClient operatorServiceClient;
    private final SessionServiceClient sessionServiceClient;

    @Value("${auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${auth.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    @Value("${auth.launch-url-base:http://localhost:8080/game}")
    private String launchUrlBase;

    /**
     * Authenticate player and return APIResponse with LoginResponse in result or error status.
     * Does not throw; returns APIResponse with appropriate StatusCode.
     */
    @Transactional
    public APIResponse login(String operatorCode, String username, String password) {
        log.info("Login attempt for operator: {}, username: {}", operatorCode, username);

        Map<String, Object> operator = operatorServiceClient.getOperatorByCode(operatorCode).block();

        if (operator == null) {
            log.warn("Invalid operator code: {}", operatorCode);
            return APIResponse.get(StatusCode.INVALID_REQUEST);
        }

        Long operatorId = ((Number) operator.get("id")).longValue();
        String operatorStatus = (String) operator.get("status");

        if (!"ACTIVE".equals(operatorStatus)) {
            log.warn("Operator not active: {}", operatorCode);
            return APIResponse.get(StatusCode.INVALID_REQUEST);
        }

        Optional<Player> playerOpt = playerRepository.findByOperatorIdAndUsername(operatorId, username);

        Player player;
        boolean isNewPlayer = false;

        if (playerOpt.isEmpty()) {
            log.info("Player not found, creating new player: operator={}, username={}", operatorCode, username);
            player = createNewPlayer(operatorId, username, password);
            isNewPlayer = true;
        } else {
            player = playerOpt.get();

            if (player.isLocked()) {
                log.warn("Account locked: playerId={}", player.getId());
                return APIResponse.get(StatusCode.ACCOUNT_BLOCKED);
            }

            if (!player.isActive()) {
                log.warn("Account not active: playerId={}", player.getId());
                return APIResponse.get(StatusCode.ACCOUNT_BLOCKED);
            }

            if (!passwordEncoder.matches(password, player.getPasswordHash())) {
                handleFailedLogin(player);
                log.warn("Invalid password for player: {}", player.getId());
                return APIResponse.get(StatusCode.INVALID_CREDENTIALS);
            }
        }

        player.setFailedLoginAttempts(0);
        player.setLockedUntil(null);
        player.setLastLoginAt(LocalDateTime.now());
        playerRepository.save(player);

        String accessToken = jwtTokenService.generateAccessToken(
            player.getId(),
            player.getOperatorId(),
            player.getUsername()
        );
        String refreshToken = jwtTokenService.generateRefreshToken(
            player.getId(),
            player.getOperatorId()
        );

        if (isNewPlayer) {
            log.info("New player created and logged in: playerId={}, operator: {}", player.getPlayerId(), operatorCode);
        } else {
            log.info("Login successful for existing player: playerId={}, operator: {}", player.getId(), operatorCode);
        }

        Long numericPlayerId = player.getId();

        LoginResponse loginResponse = LoginResponse.builder()
            .success(true)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(3600L)
            .playerId(numericPlayerId)
            .username(player.getUsername())
            .operatorCode(operatorCode)
            .timestamp(LocalDateTime.now())
            .build();

        return APIResponse.success(loginResponse);
    }

    private Player createNewPlayer(Long operatorId, String username, String password) {
        Player player = new Player();
        player.setOperatorId(operatorId);
        player.setUsername(username);
        player.setPasswordHash(passwordEncoder.encode(password));

        String playerId = String.format("player_%d_%d_%s",
            operatorId,
            System.currentTimeMillis(),
            java.util.UUID.randomUUID().toString().substring(0, 8));
        player.setPlayerId(playerId);

        player.setStatus("ACTIVE");
        player.setFailedLoginAttempts(0);

        return playerRepository.save(player);
    }

    private void handleFailedLogin(Player player) {
        int attempts = player.getFailedLoginAttempts() + 1;
        player.setFailedLoginAttempts(attempts);

        if (attempts >= maxLoginAttempts) {
            player.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            log.warn("Account locked due to failed attempts: playerId={}, attempts={}", player.getId(), attempts);
        }

        playerRepository.save(player);
    }

    /**
     * Refresh access token. Returns APIResponse with result or error status; does not throw.
     */
    @Transactional
    public APIResponse refreshToken(String refreshToken) {
        log.debug("Refreshing token");

        if (!jwtTokenService.validateToken(refreshToken)) {
            return APIResponse.get(StatusCode.INVALID_TOKEN);
        }

        String tokenType = jwtTokenService.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            return APIResponse.get(StatusCode.INVALID_TOKEN);
        }

        Long playerId = jwtTokenService.extractPlayerId(refreshToken);
        Long operatorId = jwtTokenService.extractOperatorId(refreshToken);
        String username = jwtTokenService.extractUsername(refreshToken);

        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty() || !playerOpt.get().isActive()) {
            return APIResponse.get(StatusCode.INVALID_TOKEN);
        }

        Player player = playerOpt.get();

        String newAccessToken = jwtTokenService.generateAccessToken(
            player.getId(),
            player.getOperatorId(),
            player.getUsername()
        );

        log.debug("Token refreshed for player: {}", playerId);

        RefreshTokenResponse refreshTokenResponse = RefreshTokenResponse.builder()
            .success(true)
            .accessToken(newAccessToken)
            .tokenType("Bearer")
            .expiresIn(3600L)
            .timestamp(LocalDateTime.now())
            .build();

        return APIResponse.success(refreshTokenResponse);
    }

    /**
     * Validate token and return user info. Returns APIResponse with result or error status; does not throw.
     */
    public APIResponse validateToken(String token) {
        if (!jwtTokenService.validateToken(token)) {
            return APIResponse.get(StatusCode.INVALID_TOKEN);
        }

        Long playerId = jwtTokenService.extractPlayerId(token);
        Long operatorId = jwtTokenService.extractOperatorId(token);
        String username = jwtTokenService.extractUsername(token);

        java.util.Date expirationDate = jwtTokenService.extractExpiration(token);
        java.util.Date issuedAtDate = jwtTokenService.extractAllClaims(token).getIssuedAt();

        LocalDateTime expiresAt = expirationDate != null
            ? LocalDateTime.ofInstant(expirationDate.toInstant(), java.time.ZoneId.systemDefault()) : null;
        LocalDateTime issuedAt = issuedAtDate != null
            ? LocalDateTime.ofInstant(issuedAtDate.toInstant(), java.time.ZoneId.systemDefault()) : null;

        UserInfoResponse userInfo = UserInfoResponse.builder()
            .valid(true)
            .playerId(playerId)
            .username(username)
            .operatorCode(String.valueOf(operatorId))
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .build();

        return APIResponse.success(userInfo);
    }

    /**
     * Casino launch flow: validate X-Api-Key, find/create player (no password), create session (DB + Redis), return launch URL.
     * Used when players come from operator/casino site; we validate operator (API key). Optional: currency, language, IP, table from operator config.
     */
    @Transactional
    public APIResponse getLaunchUrl(CasinoLoginRequest loginRequest, String apiKey, String clientIp, String userAgent) {
        log.info("Launch URL requested for username: {}, tableId: {}", loginRequest.getUsername(), loginRequest.getTableId());

        Optional<Long> operatorIdOpt = operatorServiceClient.validateApiKey(apiKey);
        if (operatorIdOpt.isEmpty()) {
            log.warn("Invalid or missing X-Api-Key");
            return APIResponse.get(StatusCode.INVALID_CASINO);
        }
        Long operatorId = operatorIdOpt.get();

        Map<String, Object> operator = operatorServiceClient.getOperator(operatorId);
        if (operator == null || !"ACTIVE".equals(operator.get("status"))) {
            log.warn("Operator not found or not active: {}", operatorId);
            return APIResponse.get(StatusCode.INVALID_CASINO);
        }

        // Optional: validate currency, language, IP, table from operator config (extend as needed)
        String defaultCurrency = operator.get("defaultCurrency") != null ? operator.get("defaultCurrency").toString() : "USD";

        Optional<Player> playerOpt = playerRepository.findByOperatorIdAndUsername(operatorId, loginRequest.getUsername());
        Player player;
        if (playerOpt.isEmpty()) {
            log.info("Creating new player for casino flow: operator={}, username={}", operatorId, loginRequest.getUsername());
            player = createNewPlayerForCasino(operatorId, loginRequest.getUsername());
        } else {
            player = playerOpt.get();
            if (player.isLocked() || !player.isActive()) {
                log.warn("Player locked or inactive: {}", player.getId());
                return APIResponse.get(StatusCode.ACCOUNT_BLOCKED);
            }
        }

        player.setLastLoginAt(LocalDateTime.now());
        playerRepository.save(player);

        String accessToken = jwtTokenService.generateAccessToken(player.getId(), player.getOperatorId(), player.getUsername());
        String refreshToken = jwtTokenService.generateRefreshToken(player.getId(), player.getOperatorId());

        SessionServiceClient.CreateSessionRequestDto sessionRequest = SessionServiceClient.CreateSessionRequestDto.builder()
            .playerId(player.getId())
            .operatorId(operatorId)
            .jwtToken(accessToken)
            .refreshToken(refreshToken)
            .ipAddress(clientIp)
            .userAgent(userAgent)
            .build();

        Map<String, Object> sessionResult = sessionServiceClient.createSession(sessionRequest);
        if (sessionResult == null || !sessionResult.containsKey("sessionId")) {
            log.error("Failed to create session for player: {}", player.getId());
            return APIResponse.get(StatusCode.INTERNAL_SERVER_ERROR, "Session creation failed");
        }

        String sessionId = String.valueOf(sessionResult.get("sessionId"));
        String token = accessToken;
        String url = launchUrlBase + (launchUrlBase.contains("?") ? "&" : "?") + "token=" + token;

        GameLaunchUrlResponse gameLaunchUrlResponse = GameLaunchUrlResponse.builder()
            .url(url)
            .token(sessionId)
            .build();

        log.info("Launch URL created for player: {}, sessionId: {}", player.getUsername(), sessionId);
        return APIResponse.success(gameLaunchUrlResponse);
    }

    private Player createNewPlayerForCasino(Long operatorId, String username) {
        Player player = new Player();
        player.setOperatorId(operatorId);
        player.setUsername(username);
        player.setPasswordHash(passwordEncoder.encode(CASINO_NO_PASSWORD_PLACEHOLDER));

        String playerId = String.format("player_%d_%d_%s",
            operatorId,
            System.currentTimeMillis(),
            java.util.UUID.randomUUID().toString().substring(0, 8));
        player.setPlayerId(playerId);
        player.setStatus("ACTIVE");
        player.setFailedLoginAttempts(0);
        return playerRepository.save(player);
    }
}
