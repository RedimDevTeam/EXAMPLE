package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for setting operator maintenance mode.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequest {
    
    private Boolean enabled; // true = enable maintenance, false = disable
    
    private LocalDateTime startTime; // Scheduled start time (optional, null = immediate)
    
    private LocalDateTime endTime; // Scheduled end time (optional, null = indefinite)
    
    @Size(max = 1000, message = "Maintenance message must not exceed 1000 characters")
    private String message; // Maintenance message for players
}
