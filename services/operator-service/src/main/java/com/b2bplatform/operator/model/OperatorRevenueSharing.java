package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for revenue sharing configuration.
 */
@Entity
@Table(name = "operator_revenue_sharing",
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "parent_operator_id", "revenue_type", "effective_from"}))
@Data
public class OperatorRevenueSharing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "parent_operator_id", nullable = false)
    private Long parentOperatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_operator_id", insertable = false, updatable = false)
    private Operator parentOperator;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "revenue_type", nullable = false, length = 50)
    private RevenueType revenueType;
    
    @Column(name = "parent_share_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal parentSharePercentage; // 0-100
    
    @Column(name = "operator_share_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal operatorSharePercentage; // 0-100
    
    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    
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
        if (effectiveFrom == null) {
            effectiveFrom = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
