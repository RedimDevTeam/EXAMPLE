package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "operators")
@Data
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE
    
    @Column(name = "base_currency", length = 3)
    private String baseCurrency = "USD";
    
    @Column(name = "base_language", length = 10)
    private String baseLanguage = "en";
    
    @Column(length = 20)
    private String environment; // LIVE, UAT, STAGING, DEMO
    
    @Column(name = "integration_type", length = 20)
    private String integrationType; // SHARED_WALLET, FUND_TRANSFER, AMS
    
    @Column(name = "maintenance_mode", nullable = false)
    private Boolean maintenanceMode = false;
    
    @Column(name = "maintenance_start_time")
    private LocalDateTime maintenanceStartTime;
    
    @Column(name = "maintenance_end_time")
    private LocalDateTime maintenanceEndTime;
    
    @Column(name = "maintenance_message", length = 1000)
    private String maintenanceMessage; // Maintenance message for players
    
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
