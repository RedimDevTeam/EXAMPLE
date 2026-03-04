package com.b2bplatform.session.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_player_id", columnList = "player_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false, length = 100)
    private String sessionId;
    
    @Column(name = "player_id", nullable = false)
    private Long playerId;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "jwt_token", columnDefinition = "TEXT", nullable = false)
    private String jwtToken;
    
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, ENDED
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = generateSessionId();
        }
        if (expiresAt == null) {
            expiresAt = createdAt.plusHours(24); // Default 24 hours
        }
        lastAccessedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        // Update last accessed timestamp on access
    }
    
    private String generateSessionId() {
        return "sess_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status) && !isExpired();
    }
    
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }
}
