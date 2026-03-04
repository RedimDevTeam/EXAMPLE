package com.b2bplatform.b2c.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * B2C Provider Transaction Entity
 * 
 * Tracks all wallet operations (debit/credit/refund/cancel) with B2C providers.
 * Uses industry-standard field naming conventions.
 */
@Entity
@Table(name = "provider_transactions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider_id", "transaction_id"}),
       indexes = {
           @Index(name = "idx_provider_player", columnList = "providerId,playerId"),
           @Index(name = "idx_provider_status", columnList = "providerId,status"),
           @Index(name = "idx_provider_txn_id", columnList = "providerId,transactionId"),
           @Index(name = "idx_provider_round_id", columnList = "roundId"),
           @Index(name = "idx_provider_game_id", columnList = "gameId")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;
    
    @Column(name = "player_id", nullable = false, length = 100)
    private String playerId;
    
    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;
    
    @Column(name = "transaction_subtype_id", nullable = false)
    private Integer transactionSubtypeId; // Industry standard (300-307)
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 10)
    private UnitType unitType;
    
    @Column(name = "player_level")
    private Integer playerLevel;
    
    @Column(name = "game_id", length = 100)
    private String gameId; // Industry standard naming
    
    @Column(name = "round_id", length = 100)
    private String roundId;
    
    @Column(name = "hand_id", length = 100)
    private String handId;
    
    @Column(name = "brand_id", length = 100)
    private String brandId;
    
    @Column(name = "agent_id", length = 100)
    private String agentId;
    
    @Column(name = "language", length = 10)
    private String language;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_response", columnDefinition = "jsonb")
    private Object providerResponse; // JSON response from provider
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark transaction as completed
     */
    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark transaction as failed
     */
    public void markFailed(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Unit type enum
     */
    public enum UnitType {
        CENTS,
        DECIMAL
    }
    
    /**
     * Transaction status enum
     */
    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}
