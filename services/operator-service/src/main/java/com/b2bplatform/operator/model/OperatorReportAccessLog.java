package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for report access audit log.
 */
@Entity
@Table(name = "operator_report_access_log")
@Data
public class OperatorReportAccessLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "user_identifier", nullable = false, length = 100)
    private String userIdentifier;
    
    @Column(name = "report_type", nullable = false, length = 100)
    private String reportType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_role", nullable = false, length = 20)
    private ReportRole reportRole;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_method", nullable = false, length = 20)
    private AccessMethod accessMethod;
    
    @Column(name = "accessed_at")
    private LocalDateTime accessedAt;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @PrePersist
    protected void onCreate() {
        accessedAt = LocalDateTime.now();
    }
    
    public enum AccessMethod {
        VIEW,       // Viewed report
        EXPORT,     // Exported report
        SCHEDULE,   // Scheduled report
        DOWNLOAD    // Downloaded report
    }
}
