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
public class OperatorGameConfigResponse {
    
    private Long id;
    private Long operatorId;
    private String gameProviderId;
    private String gameId;
    private Boolean isEnabled;
    private Boolean isActive;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String launchUrl;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
