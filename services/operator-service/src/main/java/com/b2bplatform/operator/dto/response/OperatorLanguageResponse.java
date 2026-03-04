package com.b2bplatform.operator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorLanguageResponse {
    
    private Long id;
    private Long operatorId;
    private String languageCode;
    private Boolean isCustom;
    private String languageName;
    private Boolean isDefault;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
