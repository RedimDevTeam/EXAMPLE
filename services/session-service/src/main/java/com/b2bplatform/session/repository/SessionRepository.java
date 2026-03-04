package com.b2bplatform.session.repository;

import com.b2bplatform.session.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findBySessionId(String sessionId);
    
    List<Session> findByPlayerId(Long playerId);
    
    List<Session> findByPlayerIdAndStatus(Long playerId, String status);
    
    @Modifying
    @Query("UPDATE Session s SET s.status = 'EXPIRED' WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    int expireSessions(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Session s WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    List<Session> findExpiredSessions(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE Session s SET s.lastAccessedAt = :timestamp WHERE s.id = :id")
    void updateLastAccessed(@Param("id") Long id, @Param("timestamp") LocalDateTime timestamp);
}
