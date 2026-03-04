package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for operator IP whitelist configuration.
 * Supports multiple IP addresses per operator with endpoint-specific access control.
 */
@Entity
@Table(name = "operator_ip_whitelist", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "ip_address"}))
@Data
public class OperatorIpWhitelist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "ip_address", nullable = false, length = 45) // Supports IPv6 (max 45 chars)
    private String ipAddress;
    
    @Column(name = "allowed_endpoints", columnDefinition = "TEXT[]") // Array of endpoint patterns
    private String[] allowedEndpoints; // e.g., ["/api/v1/bets", "/api/v1/wallet/debit"]
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
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
