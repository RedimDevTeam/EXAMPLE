package com.b2bplatform.wallet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Wallet Transaction entity with complete audit trail.
 * Tracks balance before/after for complete audit capability.
 */
@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_player_id", columnList = "player_id"),
    @Index(name = "idx_operator_id", columnList = "operator_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_reference", columnList = "reference"),
    @Index(name = "idx_related_transaction", columnList = "related_transaction_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "player_id", nullable = false, length = 100)
    private String playerId;
    
    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    /**
     * CRITICAL FOR AUDIT: Balance before transaction
     * Nullable for Shared Wallet model (balance maintained at operator)
     */
    @Column(name = "balance_before", precision = 19, scale = 2)
    private BigDecimal balanceBefore;
    
    /**
     * CRITICAL FOR AUDIT: Balance after transaction
     * Nullable for Shared Wallet model (balance maintained at operator)
     */
    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(length = 3, nullable = false)
    private String currency;
    
    /**
     * External system reference (e.g., bet ID, payment gateway transaction ID)
     */
    @Column(name = "reference", length = 255)
    private String reference;
    
    /**
     * Link to related transaction (e.g., WIN links to BET, REFUND links to BET)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_transaction_id")
    private WalletTransaction relatedTransaction;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L; // Optimistic locking
    
    // ============================================================================
    // Extended Fields for B2B Integration (Industry Standard Naming)
    // ============================================================================
    
    /**
     * Player level / Bet limit tier (0=Low, 1=Regular, 2=High, 3=VIP)
     */
    @Column(name = "player_level")
    private Integer playerLevel;
    
    /**
     * Unit type for amount (CENTS or DECIMAL)
     */
    @Column(name = "unit_type", length = 10)
    private String unitType; // CENTS or DECIMAL
    
    /**
     * Game round reference identifier
     */
    @Column(name = "round_id", length = 100)
    private String roundId;
    
    /**
     * Game identifier (Industry standard naming - was game_key)
     */
    @Column(name = "game_id", length = 100)
    private String gameId;
    
    /**
     * Game hand identifier (for card games)
     */
    @Column(name = "hand_id", length = 100)
    private String handId;
    
    /**
     * Transaction subtype code (300-307) (Industry standard naming - was txn_sub_type_id)
     */
    @Column(name = "transaction_subtype_id")
    private Integer transactionSubtypeId;
    
    /**
     * Brand/skin identifier (Industry standard naming - was skin_id)
     */
    @Column(name = "brand_id", length = 100)
    private String brandId;
    
    /**
     * Agent system reference
     */
    @Column(name = "agent_id", length = 100)
    private String agentId;
    
    /**
     * ISO 639-1 language code (Industry standard naming - was lang)
     */
    @Column(name = "language", length = 10)
    private String language;
    
    /**
     * Transaction Status
     */
    public enum TransactionStatus {
        PENDING,     // Transaction initiated but not completed
        COMPLETED,   // Transaction successfully processed
        FAILED,      // Transaction failed
        CANCELLED,   // Transaction cancelled
        RETRYING     // Transaction is being retried
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = generateTransactionId();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == TransactionStatus.COMPLETED || status == TransactionStatus.FAILED) {
            if (completedAt == null) {
                completedAt = LocalDateTime.now();
            }
        }
    }
    
    private String generateTransactionId() {
        return "txn_" + UUID.randomUUID().toString().replace("-", "");
    }
}
