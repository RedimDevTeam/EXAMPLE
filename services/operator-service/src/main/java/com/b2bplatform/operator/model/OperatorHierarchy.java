package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for operator hierarchy (Master → Agent → Sub-Agent).
 */
@Entity
@Table(name = "operator_hierarchy",
       uniqueConstraints = @UniqueConstraint(columnNames = "operator_id"))
@Data
public class OperatorHierarchy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false, unique = true)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "parent_operator_id")
    private Long parentOperatorId; // null for Master operators
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_operator_id", insertable = false, updatable = false)
    private Operator parentOperator;
    
    @Column(name = "hierarchy_level", nullable = false)
    private Integer hierarchyLevel; // 1=Master, 2=Agent, 3=Sub-Agent
    
    @Column(name = "hierarchy_path", length = 500)
    private String hierarchyPath; // e.g., "1/5/10"
    
    @Column(name = "is_master", nullable = false)
    private Boolean isMaster = false;
    
    @Column(name = "is_agent", nullable = false)
    private Boolean isAgent = false;
    
    @Column(name = "is_sub_agent", nullable = false)
    private Boolean isSubAgent = false;
    
    @Column(name = "can_create_children", nullable = false)
    private Boolean canCreateChildren = false;
    
    @Column(name = "max_children_count")
    private Integer maxChildrenCount; // null = unlimited
    
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
