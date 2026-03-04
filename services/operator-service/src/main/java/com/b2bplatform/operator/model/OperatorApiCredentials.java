package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for API access credentials (username/password).
 */
@Entity
@Table(name = "operator_api_credentials",
       uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Data
public class OperatorApiCredentials {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // BCrypt hashed password
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false;
    
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (passwordChangedAt == null) {
            passwordChangedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
