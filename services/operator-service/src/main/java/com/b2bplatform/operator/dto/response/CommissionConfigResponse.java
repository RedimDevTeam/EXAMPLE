package com.b2bplatform.operator.dto.response;

import com.b2bplatform.operator.model.CommissionModelType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for commission configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommissionConfigResponse {
    
    private Long id;
    private Long operatorId;
    private String gameProviderId;
    private String gameId;
    private CommissionModelType commissionModel;
    
    // GGR-Based fields
    private BigDecimal operatorGgrRate;
    private BigDecimal providerGgrRate;
    
    // Fixed Price fields
    private BigDecimal fixedPricePerBet;
    private String fixedPriceCurrency;
    
    // Winnings-Based fields
    private BigDecimal winningsCommissionRate;
    private BigDecimal operatorWinningsShare;
    private BigDecimal providerWinningsShare;
    
    private Boolean isActive;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
