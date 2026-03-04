package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for operator-specific bet limits.
 * These limits override game-specific limits for a specific operator.
 */
@Entity
@Table(name = "operator_game_bet_limits")
@Data
public class OperatorGameBetLimit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "game_id", nullable = false, length = 100)
    private String gameId;
    
    @Column(name = "game_provider_id", nullable = false, length = 100)
    private String gameProviderId;
    
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    
    @Column(name = "min_bet", nullable = false, precision = 19, scale = 2)
    private BigDecimal minBet;
    
    @Column(name = "max_bet", nullable = false, precision = 19, scale = 2)
    private BigDecimal maxBet;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    
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
        if (effectiveFrom == null) {
            effectiveFrom = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
