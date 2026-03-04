package com.b2bplatform.b2c.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * B2C Provider Configuration Entity
 * 
 * Stores configuration for B2C providers who manage their own wallets.
 * Includes API endpoints, authentication credentials, and provider capabilities.
 */
@Entity
@Table(name = "provider_config", indexes = {
    @Index(name = "idx_provider_active", columnList = "isActive"),
    @Index(name = "idx_provider_id", columnList = "providerId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "provider_id", unique = true, nullable = false, length = 100)
    private String providerId;
    
    @Column(name = "provider_name", nullable = false, length = 200)
    private String providerName;
    
    @Column(name = "api_base_url", nullable = false, length = 500)
    private String apiBaseUrl;
    
    @Column(name = "api_key", nullable = false, length = 255)
    private String apiKey;
    
    @Column(name = "api_secret", length = 255)
    private String apiSecret; // For HMAC signature
    
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 50)
    @Builder.Default
    private AuthType authType = AuthType.API_KEY;
    
    @Column(name = "supports_xml")
    @Builder.Default
    private Boolean supportsXml = false;
    
    @Column(name = "supports_json")
    @Builder.Default
    private Boolean supportsJson = true;
    
    @Column(name = "timeout_ms")
    @Builder.Default
    private Integer timeoutMs = 5000;
    
    @Column(name = "retry_attempts")
    @Builder.Default
    private Integer retryAttempts = 3;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Authentication type enum
     */
    public enum AuthType {
        API_KEY,
        HMAC,
        OAUTH
    }
}
