package com.b2bplatform.operator.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for operator maintenance status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceStatusResponse {
    
    private Long operatorId;
    private Boolean isInMaintenance;
    private LocalDateTime maintenanceStartTime;
    private LocalDateTime maintenanceEndTime;
    private String maintenanceMessage;
    private Boolean isScheduled; // true if maintenance is scheduled for future
    private Long minutesRemaining; // Minutes until maintenance starts/ends (if scheduled)
}
