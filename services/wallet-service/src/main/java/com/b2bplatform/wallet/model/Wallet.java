package com.b2bplatform.wallet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet entity for tracking player wallet balances.
 * Supports both Shared Wallet and Fund Transfer models.
 */
@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_operator_player", columnList = "operator_id,player_id"),
    @Index(name = "idx_wallet_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "player_id", nullable = false, length = 100)
    private String playerId;
    
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_model", nullable = false, length = 20)
    @Builder.Default
    private WalletModel walletModel = WalletModel.SHARED_WALLET; // SHARED_WALLET or FUND_TRANSFER
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;
    
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L; // Optimistic locking
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Wallet Model Types
     */
    public enum WalletModel {
        SHARED_WALLET,    // Operator holds wallet, B2B requests funds seamlessly
        FUND_TRANSFER     // Wallet maintained at B2B, player transfers funds from operator
    }
    
    /**
     * Wallet Status
     */
    public enum WalletStatus {
        ACTIVE,   // Wallet is active and can process transactions
        FROZEN,   // Wallet is frozen (no transactions allowed)
        CLOSED    // Wallet is closed
    }
}
