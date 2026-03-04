package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for operator chip denominations.
 * Supports flexible chip counts (e.g., 5, 6, 7 chips) per operator/game/currency.
 * Chip index starts at 0 and can be any non-negative integer based on UI space availability.
 */
@Entity
@Table(name = "operator_chip_denominations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "game_id", "currency_code", "chip_index"}))
@Data
public class OperatorChipDenomination {
    
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
    
    @Column(name = "chip_index", nullable = false)
    private Integer chipIndex; // Flexible: 0-based index (e.g., 0-4 for 5 chips, 0-5 for 6 chips, 0-6 for 7 chips)
    
    @Column(name = "chip_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal chipValue;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "display_order")
    private Integer displayOrder; // Order for UI display (0 = first chip shown)
    
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
        if (displayOrder == null) {
            displayOrder = chipIndex; // Default to chip index
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
