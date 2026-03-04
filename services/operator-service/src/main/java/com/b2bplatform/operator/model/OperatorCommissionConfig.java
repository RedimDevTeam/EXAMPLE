package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for operator commission configuration.
 * Defines commission models per operator-game provider combination.
 */
@Entity
@Table(name = "operator_commission_config", indexes = {
    @Index(name = "idx_commission_config_operator_id", columnList = "operator_id"),
    @Index(name = "idx_commission_config_provider_id", columnList = "game_provider_id"),
    @Index(name = "idx_commission_config_game_id", columnList = "game_id"),
    @Index(name = "idx_commission_config_active", columnList = "is_active, effective_from, effective_to")
})
@Data
public class OperatorCommissionConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "game_provider_id", nullable = false, length = 100)
    private String gameProviderId;
    
    @Column(name = "game_id", length = 100)
    private String gameId; // NULL = all games from provider
    
    @Enumerated(EnumType.STRING)
    @Column(name = "commission_model", nullable = false, length = 50)
    private CommissionModelType commissionModel;
    
    // GGR-Based Commission Fields
    @Column(name = "operator_ggr_rate", precision = 5, scale = 2)
    private BigDecimal operatorGgrRate; // Percentage (0.00-100.00)
    
    @Column(name = "provider_ggr_rate", precision = 5, scale = 2)
    private BigDecimal providerGgrRate; // Percentage (0.00-100.00)
    
    // Fixed Price per Bet Fields
    @Column(name = "fixed_price_per_bet", precision = 19, scale = 2)
    private BigDecimal fixedPricePerBet;
    
    @Column(name = "fixed_price_currency", length = 3)
    private String fixedPriceCurrency;
    
    // Winnings-Based Commission Fields
    @Column(name = "winnings_commission_rate", precision = 5, scale = 2)
    private BigDecimal winningsCommissionRate; // Percentage of winnings
    
    @Column(name = "operator_winnings_share", precision = 5, scale = 2)
    private BigDecimal operatorWinningsShare; // Percentage of commission
    
    @Column(name = "provider_winnings_share", precision = 5, scale = 2)
    private BigDecimal providerWinningsShare; // Percentage of commission
    
    // Common Fields
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo; // NULL = currently active
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy; // Gaming Provider Global Admin username
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
