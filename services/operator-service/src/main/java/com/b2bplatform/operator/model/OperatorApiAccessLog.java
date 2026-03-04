package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for tracking all API access to operator endpoints.
 */
@Entity
@Table(name = "operator_api_access_log", indexes = {
    @Index(name = "idx_api_access_operator_id", columnList = "operator_id"),
    @Index(name = "idx_api_access_endpoint", columnList = "endpoint"),
    @Index(name = "idx_api_access_created_at", columnList = "created_at"),
    @Index(name = "idx_api_access_status", columnList = "http_status"),
    @Index(name = "idx_api_access_operator_endpoint", columnList = "operator_id,endpoint")
})
@Data
public class OperatorApiAccessLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id")
    private Long operatorId; // NULL if endpoint doesn't require operator context
    
    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint; // e.g., /api/v1/admin/operators/5/maintenance
    
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod; // GET, POST, PUT, DELETE
    
    @Column(name = "http_status", nullable = false)
    private Integer httpStatus; // 200, 400, 500, etc.
    
    @Column(name = "request_ip", nullable = false, length = 45)
    private String requestIp; // IP address of the requester
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "authenticated_by", length = 100)
    private String authenticatedBy; // API key identifier or username
    
    @Column(name = "request_id", length = 100)
    private String requestId; // Unique request identifier for tracing
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs; // Response time in milliseconds
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage; // Error message if request failed
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
