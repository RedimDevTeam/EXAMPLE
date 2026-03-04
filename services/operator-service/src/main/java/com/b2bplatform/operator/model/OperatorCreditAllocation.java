package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for credit allocation from parent to child operators.
 */
@Entity
@Table(name = "operator_credit_allocation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"parent_operator_id", "child_operator_id", "currency_code"}))
@Data
public class OperatorCreditAllocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "parent_operator_id", nullable = false)
    private Long parentOperatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_operator_id", insertable = false, updatable = false)
    private Operator parentOperator;
    
    @Column(name = "child_operator_id", nullable = false)
    private Long childOperatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_operator_id", insertable = false, updatable = false)
    private Operator childOperator;
    
    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;
    
    @Column(name = "used_credit", nullable = false, precision = 19, scale = 2)
    private BigDecimal usedCredit = BigDecimal.ZERO;
    
    @Column(name = "available_credit", precision = 19, scale = 2, insertable = false, updatable = false)
    private BigDecimal availableCredit; // Generated column: credit_limit - used_credit
    
    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_type", nullable = false, length = 20)
    private AllocationType allocationType = AllocationType.MANUAL;
    
    @Column(name = "auto_replenish", nullable = false)
    private Boolean autoReplenish = false;
    
    @Column(name = "replenish_threshold", precision = 5, scale = 2)
    private BigDecimal replenishThreshold; // Percentage (e.g., 20.00 = 20%)
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
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
        if (allocatedAt == null) {
            allocatedAt = LocalDateTime.now();
        }
        if (usedCredit == null) {
            usedCredit = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
