package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity for report access management (hierarchical roles).
 */
@Entity
@Table(name = "operator_report_access",
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "user_identifier"}))
@Data
public class OperatorReportAccess {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "user_identifier", nullable = false, length = 100)
    private String userIdentifier; // Username or user ID
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_role", nullable = false, length = 20)
    private ReportRole reportRole;
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_report_types", columnDefinition = "text[]")
    private List<String> allowedReportTypes; // Array of report type names
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_operators", columnDefinition = "bigint[]")
    private List<Long> allowedOperators; // Array of operator IDs
    
    @Column(name = "can_view_all_operators", nullable = false)
    private Boolean canViewAllOperators = false;
    
    @Column(name = "can_export_reports", nullable = false)
    private Boolean canExportReports = true;
    
    @Column(name = "can_schedule_reports", nullable = false)
    private Boolean canScheduleReports = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 20)
    private AccessLevel accessLevel = AccessLevel.READ_ONLY;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
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
