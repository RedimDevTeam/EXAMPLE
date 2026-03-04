package com.b2bplatform.session.service;

import com.b2bplatform.session.model.Session;
import com.b2bplatform.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to clean up expired sessions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupService {
    
    private final SessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String REDIS_KEY_PREFIX = "session:";
    
    /**
     * Clean up expired sessions
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredSessions() {
        log.info("Starting session cleanup job");
        
        LocalDateTime now = LocalDateTime.now();
        List<Session> expiredSessions = sessionRepository.findExpiredSessions(now);
        
        log.info("Found {} expired sessions", expiredSessions.size());
        
        for (Session session : expiredSessions) {
            // Mark as expired in database
            session.setStatus("EXPIRED");
            sessionRepository.save(session);
            
            // Remove from Redis
            String redisKey = REDIS_KEY_PREFIX + session.getSessionId();
            redisTemplate.delete(redisKey);
            
            log.debug("Cleaned up expired session: {}", session.getSessionId());
        }
        
        log.info("Session cleanup completed. Processed {} sessions", expiredSessions.size());
    }
}
