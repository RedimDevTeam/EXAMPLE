package com.b2bplatform.operator.dto.response;

import com.b2bplatform.operator.model.FileFormat;
import com.b2bplatform.operator.model.LogoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandingResponse {
    
    private Long id;
    private Long operatorId;
    private LogoType logoType;
    private String fileName;
    private String filePath; // URL or path to access the file
    private FileFormat fileFormat;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private Boolean isActive;
    private Integer displayOrder;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    // Additional fields for game provider and game branding
    private String gameProviderId;
    private String gameId;
}
