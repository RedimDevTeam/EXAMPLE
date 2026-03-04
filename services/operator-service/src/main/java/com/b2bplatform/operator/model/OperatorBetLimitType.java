package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for operator bet limit types.
 * Supports Standard, VIP, Promotional, and Custom bet limit types.
 */
@Entity
@Table(name = "operator_bet_limit_types",
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "game_id", "currency_code", "limit_type"}))
@Data
public class OperatorBetLimitType {
    
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
    
    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false, length = 20)
    private BetLimitType limitType;
    
    @Column(name = "min_bet_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal minBetLimit;
    
    @Column(name = "max_bet_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal maxBetLimit;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
