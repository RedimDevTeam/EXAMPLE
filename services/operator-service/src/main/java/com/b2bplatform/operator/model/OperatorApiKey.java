package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "operator_api_keys")
@Data
public class OperatorApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "api_key", unique = true, nullable = false, length = 100)
    private String apiKey;
    
    @Column(name = "key_name", length = 100)
    private String keyName; // Optional name for the key
    
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, REVOKED
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Optional expiration
    
    @Column(name = "ip_whitelist")
    private String ipWhitelist; // Comma-separated IPs (optional)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (apiKey == null || apiKey.isEmpty()) {
            // Generate API key if not provided
            apiKey = generateApiKey();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateApiKey() {
        // Format: b2b_{operator_id}_{uuid_hash}
        String hash = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return String.format("b2b_%d_%s", operatorId, hash);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status) && !isExpired();
    }
}
