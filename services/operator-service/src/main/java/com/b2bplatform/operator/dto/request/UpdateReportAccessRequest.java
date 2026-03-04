package com.b2bplatform.operator.dto.request;

import com.b2bplatform.operator.model.AccessLevel;
import com.b2bplatform.operator.model.ReportRole;
import lombok.Data;

import java.util.List;

@Data
public class UpdateReportAccessRequest {
    
    private ReportRole reportRole;
    
    private List<String> allowedReportTypes;
    
    private List<Long> allowedOperators;
    
    private Boolean canViewAllOperators;
    
    private Boolean canExportReports;
    
    private Boolean canScheduleReports;
    
    private AccessLevel accessLevel;
    
    private Boolean isActive;
}
