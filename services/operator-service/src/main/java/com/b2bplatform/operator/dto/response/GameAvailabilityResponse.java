package com.b2bplatform.operator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for checking game availability (used by Bet Service).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAvailabilityResponse {
    
    private Long operatorId;
    private String gameProviderId;
    private String gameId;
    private Boolean isEnabled;
    private Boolean isAvailable; // isEnabled AND isActive AND within effective dates
    private String launchUrl;
}
