package com.b2bplatform.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtTokenService {
    
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    
    public JwtTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration * 1000; // Convert to milliseconds
        this.refreshTokenExpiration = refreshTokenExpiration * 1000;
    }
    
    /**
     * Generate access token for player
     */
    public String generateAccessToken(Long playerId, Long operatorId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("player_id", playerId.toString());
        claims.put("operator_id", operatorId.toString());
        claims.put("username", username);
        claims.put("type", "access");
        
        return createToken(claims, playerId.toString(), accessTokenExpiration);
    }
    
    /**
     * Generate refresh token for player
     */
    public String generateRefreshToken(Long playerId, Long operatorId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("player_id", playerId.toString());
        claims.put("operator_id", operatorId.toString());
        claims.put("type", "refresh");
        
        return createToken(claims, playerId.toString(), refreshTokenExpiration);
    }
    
    /**
     * Create JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }
    
    /**
     * Extract all claims from token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract player ID from token
     */
    public Long extractPlayerId(String token) {
        String playerIdStr = extractClaim(token, claims -> claims.get("player_id", String.class));
        return playerIdStr != null ? Long.parseLong(playerIdStr) : null;
    }
    
    /**
     * Extract operator ID from token
     */
    public Long extractOperatorId(String token) {
        String operatorIdStr = extractClaim(token, claims -> claims.get("operator_id", String.class));
        return operatorIdStr != null ? Long.parseLong(operatorIdStr) : null;
    }
    
    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }
    
    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Validate token
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get token type (access or refresh)
     */
    public String getTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
}
