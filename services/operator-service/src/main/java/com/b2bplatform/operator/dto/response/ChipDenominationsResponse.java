package com.b2bplatform.operator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for chip denominations list (used by Bet Service).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipDenominationsResponse {
    
    private Long operatorId;
    private String gameId;
    private String currencyCode;
    private List<ChipDenominationResponse> chips; // Ordered by displayOrder
}
