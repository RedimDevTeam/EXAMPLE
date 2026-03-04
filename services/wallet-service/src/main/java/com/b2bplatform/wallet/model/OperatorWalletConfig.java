package com.b2bplatform.wallet.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "operator_wallet_config", uniqueConstraints = @UniqueConstraint(columnNames = "operator_id"))
@Data
public class OperatorWalletConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false, unique = true)
    private Long operatorId;
    
    @Column(name = "debit_url", nullable = false, length = 500)
    private String debitUrl;
    
    @Column(name = "credit_url", nullable = false, length = 500)
    private String creditUrl;
    
    @Column(name = "balance_url", nullable = false, length = 500)
    private String balanceUrl;
    
    @Column(name = "transfer_url", length = 500)
    private String transferUrl;
    
    @Column(name = "auth_type", length = 50)
    private String authType = "API_KEY";
    
    @Column(name = "auth_header", length = 100)
    private String authHeader = "X-API-Key";
    
    @Column(name = "auth_value", length = 255)
    private String authValue;
    
    @Column(name = "timeout_ms")
    private Integer timeoutMs = 5000;
    
    @Column(name = "retry_attempts")
    private Integer retryAttempts = 3;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "created_at")
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
