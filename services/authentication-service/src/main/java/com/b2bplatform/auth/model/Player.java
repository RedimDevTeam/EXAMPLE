package com.b2bplatform.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_credentials", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "username"}))
@Data
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "player_id", nullable = false, length = 100)
    private String playerId; // Unique player identifier within operator
    
    @Column(nullable = false, length = 100)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(length = 255)
    private String email;
    
    @Column(length = 50)
    private String phone;
    
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, LOCKED
    
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status) && !isLocked();
    }
}
