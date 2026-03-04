package com.b2bplatform.wallet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Player Account Status entity
 * Tracks player account status for operational APIs (block/unblock/kickout)
 */
@Entity
@Table(name = "player_account_status", indexes = {
    @Index(name = "idx_account_status", columnList = "operator_id,player_id,is_blocked"),
    @Index(name = "idx_account_blocked", columnList = "is_blocked"),
    @Index(name = "idx_account_last_activity", columnList = "last_activity_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAccountStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Operator ID
     */
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    /**
     * Player identifier
     */
    @Column(name = "player_id", nullable = false, length = 100)
    private String playerId;
    
    /**
     * Is account blocked
     */
    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private Boolean isBlocked = false;
    
    /**
     * Reason for blocking
     */
    @Column(name = "blocked_reason", length = 500)
    private String blockedReason;
    
    /**
     * When account was blocked
     */
    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;
    
    /**
     * Who blocked the account
     */
    @Column(name = "blocked_by", length = 100)
    private String blockedBy;
    
    /**
     * When account was unblocked
     */
    @Column(name = "unblocked_at")
    private LocalDateTime unblockedAt;
    
    /**
     * Who unblocked the account
     */
    @Column(name = "unblocked_by", length = 100)
    private String unblockedBy;
    
    /**
     * When player was kicked out
     */
    @Column(name = "kicked_out_at")
    private LocalDateTime kickedOutAt;
    
    /**
     * Who kicked out the player
     */
    @Column(name = "kicked_out_by", length = 100)
    private String kickedOutBy;
    
    /**
     * Last activity timestamp
     */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
    
    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Block the account
     */
    public void block(String reason, String blockedBy) {
        this.isBlocked = true;
        this.blockedReason = reason;
        this.blockedAt = LocalDateTime.now();
        this.blockedBy = blockedBy;
        this.unblockedAt = null;
        this.unblockedBy = null;
    }
    
    /**
     * Unblock the account
     */
    public void unblock(String unblockedBy) {
        this.isBlocked = false;
        this.unblockedAt = LocalDateTime.now();
        this.unblockedBy = unblockedBy;
        this.blockedReason = null;
    }
    
    /**
     * Kick out the player
     */
    public void kickout(String kickedOutBy) {
        this.kickedOutAt = LocalDateTime.now();
        this.kickedOutBy = kickedOutBy;
    }
    
    /**
     * Update last activity
     */
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
}
