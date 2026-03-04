package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for tracking operator configuration changes and administrative actions.
 */
@Entity
@Table(name = "operator_audit_log", indexes = {
    @Index(name = "idx_audit_log_operator_id", columnList = "operator_id"),
    @Index(name = "idx_audit_log_action_type", columnList = "action_type"),
    @Index(name = "idx_audit_log_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_log_operator_action", columnList = "operator_id,action_type")
})
@Data
public class OperatorAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id")
    private Long operatorId; // NULL if action is not operator-specific
    
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // OPERATOR_CREATED, OPERATOR_UPDATED, MAINTENANCE_ENABLED, IP_WHITELIST_ADDED, etc.
    
    @Column(name = "action_description", length = 500)
    private String actionDescription;
    
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy; // Username or API key identifier
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IP address of the requester
    
    @Column(name = "request_id", length = 100)
    private String requestId; // Unique request identifier for tracing
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON string of old values (before change)
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON string of new values (after change)
    
    @Column(name = "changed_fields", length = 500)
    private String changedFields; // Comma-separated list of changed field names
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
