package com.b2bplatform.operator.dto.response;

import com.b2bplatform.operator.model.AccessLevel;
import com.b2bplatform.operator.model.ReportRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportAccessResponse {
    
    private Long id;
    private Long operatorId;
    private String userIdentifier;
    private ReportRole reportRole;
    private List<String> allowedReportTypes;
    private List<Long> allowedOperators;
    private Boolean canViewAllOperators;
    private Boolean canExportReports;
    private Boolean canScheduleReports;
    private AccessLevel accessLevel;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
