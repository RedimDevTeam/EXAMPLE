package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for operator language support.
 * Allows operators to support multiple languages.
 */
@Entity
@Table(name = "operator_languages", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "language_code"}))
@Data
public class OperatorLanguage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;
    
    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false;
    
    @Column(name = "language_name", length = 100)
    private String languageName;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
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
        // Ensure language code is lowercase
        if (languageCode != null) {
            languageCode = languageCode.toLowerCase();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Ensure language code is lowercase
        if (languageCode != null) {
            languageCode = languageCode.toLowerCase();
        }
    }
}
