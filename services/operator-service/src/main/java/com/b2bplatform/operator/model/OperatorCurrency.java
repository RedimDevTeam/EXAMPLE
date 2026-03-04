package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for operator currency support.
 * Allows operators to support multiple currencies.
 */
@Entity
@Table(name = "operator_currencies", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "currency_code"}))
@Data
public class OperatorCurrency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;
    
    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false;
    
    @Column(name = "currency_name", length = 100)
    private String currencyName;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;
    
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
        // Ensure currency code is uppercase
        if (currencyCode != null) {
            currencyCode = currencyCode.toUpperCase();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Ensure currency code is uppercase
        if (currencyCode != null) {
            currencyCode = currencyCode.toUpperCase();
        }
    }
}
