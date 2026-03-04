package com.b2bplatform.operator.dto.request;

import com.b2bplatform.operator.model.AccessLevel;
import com.b2bplatform.operator.model.ReportRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateReportAccessRequest {
    
    @NotBlank(message = "User identifier is required")
    @Size(max = 100, message = "User identifier must not exceed 100 characters")
    private String userIdentifier;
    
    @NotNull(message = "Report role is required")
    private ReportRole reportRole;
    
    private List<String> allowedReportTypes; // Optional - null = all types
    
    private List<Long> allowedOperators; // Optional - null = all operators (for GLOBAL_ADMIN)
    
    private Boolean canViewAllOperators; // For GLOBAL_ADMIN
    
    private Boolean canExportReports = true;
    
    private Boolean canScheduleReports = false;
    
    private AccessLevel accessLevel = AccessLevel.READ_ONLY;
}
