package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for operator URL configuration.
 * Configures operator API base URL, directory path, and virtual path.
 */
@Entity
@Table(name = "operator_url_config", 
       uniqueConstraints = @UniqueConstraint(columnNames = "operator_id"))
@Data
public class OperatorUrlConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false, unique = true)
    private Long operatorId;
    
    @Column(name = "request_url", length = 500)
    private String requestUrl; // Operator API base URL (e.g., https://operator.com/api)
    
    @Column(name = "directory_path", length = 200)
    private String directoryPath; // Application directory mapping (e.g., /app/v1)
    
    @Column(name = "virtual_path", length = 200)
    private String virtualPath; // Reverse proxy / routing path (e.g., /operator-api)
    
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
