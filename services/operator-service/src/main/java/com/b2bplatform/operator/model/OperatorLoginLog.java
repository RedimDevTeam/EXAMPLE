package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for tracking admin login attempts and sessions.
 */
@Entity
@Table(name = "operator_login_log", indexes = {
    @Index(name = "idx_login_log_username", columnList = "username"),
    @Index(name = "idx_login_log_status", columnList = "login_status"),
    @Index(name = "idx_login_log_created_at", columnList = "created_at"),
    @Index(name = "idx_login_log_ip", columnList = "ip_address")
})
@Data
public class OperatorLoginLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    
    @Column(name = "login_status", nullable = false, length = 20)
    private String loginStatus; // SUCCESS, FAILED, LOCKED
    
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "failure_reason", length = 200)
    private String failureReason; // e.g., "Invalid password", "Account locked"
    
    @Column(name = "session_id", length = 100)
    private String sessionId; // Session identifier if login successful
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
