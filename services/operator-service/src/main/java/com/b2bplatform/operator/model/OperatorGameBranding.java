package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for game branding assets per operator.
 */
@Entity
@Table(name = "operator_game_branding",
       uniqueConstraints = @UniqueConstraint(columnNames = {"operator_id", "game_id", "logo_type", "display_order"}))
@Data
public class OperatorGameBranding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", insertable = false, updatable = false)
    private Operator operator;
    
    @Column(name = "game_id", nullable = false, length = 100)
    private String gameId;
    
    @Column(name = "game_provider_id", nullable = false, length = 100)
    private String gameProviderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "logo_type", nullable = false, length = 20)
    private LogoType logoType;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_format", nullable = false, length = 10)
    private FileFormat fileFormat;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "width")
    private Integer width;
    
    @Column(name = "height")
    private Integer height;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
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
