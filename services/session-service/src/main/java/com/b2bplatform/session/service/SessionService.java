package com.b2bplatform.session.service;

import com.b2bplatform.session.dto.request.CreateSessionRequest;
import com.b2bplatform.session.dto.response.SessionResponse;
import com.b2bplatform.session.model.Session;
import com.b2bplatform.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    
    private final SessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${session.default-timeout-hours:24}")
    private int defaultTimeoutHours;
    
    @Value("${session.refresh-on-access:true}")
    private boolean refreshOnAccess;
    
    private static final String REDIS_KEY_PREFIX = "session:";
    
    /**
     * Create a new session
     */
    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        log.info("Creating session for player: {}, operator: {}", request.getPlayerId(), request.getOperatorId());
        
        Session session = new Session();
        session.setPlayerId(request.getPlayerId());
        session.setOperatorId(request.getOperatorId());
        session.setJwtToken(request.getJwtToken());
        session.setRefreshToken(request.getRefreshToken());
        session.setStatus("ACTIVE");
        session.setIpAddress(request.getIpAddress());
        session.setUserAgent(request.getUserAgent());
        session.setExpiresAt(LocalDateTime.now().plusHours(defaultTimeoutHours));
        
        Session saved = sessionRepository.save(session);
        
        // Store in Redis for fast access
        storeInRedis(saved);
        
        log.info("Session created: {}", saved.getSessionId());
        return toResponse(saved);
    }
    
    /**
     * Validate session
     */
    @Transactional
    public Optional<SessionResponse> validateSession(String sessionId) {
        log.debug("Validating session: {}", sessionId);
        
        // Check Redis cache indicator (fast path)
        boolean existsInCache = existsInRedis(sessionId);
        if (!existsInCache) {
            log.debug("Session not in Redis cache: {}", sessionId);
            // If not in cache, might not exist - but still check DB
        }
        
        // Check database (slow path)
        Optional<Session> sessionOpt = sessionRepository.findBySessionId(sessionId);
        
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            
            if (session.isActive()) {
                // Update last accessed
                if (refreshOnAccess) {
                    updateLastAccessed(session.getId());
                }
                // Store in Redis for next time
                try {
                    storeInRedis(session);
                } catch (Exception e) {
                    log.warn("Error storing session in Redis: {}", e.getMessage());
                }
                log.debug("Session validated: {}", sessionId);
                return Optional.of(toResponse(session));
            } else {
                // Remove inactive session from cache
                removeFromRedis(sessionId);
                log.warn("Session is not active: {} (status: {}, expired: {})", 
                    sessionId, session.getStatus(), session.isExpired());
            }
        } else {
            log.warn("Session not found: {}", sessionId);
        }
        
        return Optional.empty();
    }
    
    /**
     * Refresh session (extend expiration)
     */
    @Transactional
    public Optional<SessionResponse> refreshSession(String sessionId) {
        log.debug("Refreshing session: {}", sessionId);
        
        Optional<Session> sessionOpt = sessionRepository.findBySessionId(sessionId);
        
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            
            if (session.isActive()) {
                // Extend expiration
                session.setExpiresAt(LocalDateTime.now().plusHours(defaultTimeoutHours));
                session.updateLastAccessed();
                Session updated = sessionRepository.save(session);
                
                // Update Redis
                storeInRedis(updated);
                
                log.info("Session refreshed: {}", sessionId);
                return Optional.of(toResponse(updated));
            } else {
                log.warn("Cannot refresh inactive session: {}", sessionId);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * End session (logout)
     */
    @Transactional
    public boolean endSession(String sessionId) {
        log.info("Ending session: {}", sessionId);
        
        Optional<Session> sessionOpt = sessionRepository.findBySessionId(sessionId);
        
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            session.setStatus("ENDED");
            sessionRepository.save(session);
            
            // Remove from Redis
            removeFromRedis(sessionId);
            
            log.info("Session ended: {}", sessionId);
            return true;
        }
        
        log.warn("Session not found for ending: {}", sessionId);
        return false;
    }
    
    /**
     * Get all active sessions for a player
     */
    public List<SessionResponse> getPlayerSessions(Long playerId) {
        return sessionRepository.findByPlayerIdAndStatus(playerId, "ACTIVE").stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Session entity to SessionResponse DTO
     */
    private SessionResponse toResponse(Session session) {
        return SessionResponse.builder()
            .sessionId(session.getSessionId())
            .playerId(session.getPlayerId())
            .operatorId(session.getOperatorId())
            .status(session.getStatus())
            .expiresAt(session.getExpiresAt())
            .createdAt(session.getCreatedAt())
            .lastAccessedAt(session.getLastAccessedAt())
            .build();
    }
    
    /**
     * Store session in Redis (as cache indicator)
     * We store a simple flag to indicate session exists, full data comes from DB
     */
    private void storeInRedis(Session session) {
        try {
            String key = REDIS_KEY_PREFIX + session.getSessionId();
            long ttlSeconds = java.time.Duration.between(LocalDateTime.now(), session.getExpiresAt()).getSeconds();
            
            if (ttlSeconds > 0) {
                // Store simple flag instead of full object to avoid deserialization issues
                redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS);
                log.debug("Session cached in Redis: {} (TTL: {}s)", session.getSessionId(), ttlSeconds);
            }
        } catch (Exception e) {
            log.warn("Error storing session in Redis: {}", e.getMessage());
        }
    }
    
    /**
     * Check if session exists in Redis cache
     */
    private boolean existsInRedis(String sessionId) {
        try {
            String key = REDIS_KEY_PREFIX + sessionId;
            Object value = redisTemplate.opsForValue().get(key);
            return value != null;
        } catch (Exception e) {
            log.warn("Error checking Redis cache: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get session from Redis (deprecated - using existsInRedis instead)
     * Always returns null, we use Redis only as cache indicator
     */
    @Deprecated
    private Session getFromRedis(String sessionId) {
        // Redis is used only as cache indicator, not for full object storage
        // This avoids deserialization issues
        return null;
    }
    
    /**
     * Remove session from Redis
     */
    private void removeFromRedis(String sessionId) {
        String key = REDIS_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.debug("Session removed from Redis: {}", sessionId);
    }
    
    /**
     * Update last accessed timestamp
     */
    private void updateLastAccessed(Long sessionId) {
        sessionRepository.updateLastAccessed(sessionId, LocalDateTime.now());
    }
}
