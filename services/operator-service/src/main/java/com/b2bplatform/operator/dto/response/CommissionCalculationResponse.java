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
 * Response DTO for commission calculation history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommissionCalculationResponse {
    
    private Long id;
    private Long operatorId;
    private String gameProviderId;
    private String gameId;
    private Long commissionConfigId;
    private CommissionModelType commissionModel;
    
    private LocalDateTime calculationPeriodStart;
    private LocalDateTime calculationPeriodEnd;
    
    // GGR-Based Metrics
    private BigDecimal totalBets;
    private BigDecimal totalWinnings;
    private BigDecimal ggr;
    private BigDecimal operatorCommission;
    private BigDecimal providerCommission;
    
    // Fixed Price Metrics
    private Integer numberOfBets;
    private BigDecimal fixedPricePerBet;
    private BigDecimal totalOperatorFee;
    
    // Winnings-Based Metrics
    private BigDecimal totalWinningsAmount;
    private BigDecimal winningsCommissionRate;
    private BigDecimal totalCommission;
    private BigDecimal operatorCommissionShare;
    private BigDecimal providerCommissionShare;
    
    private String currency;
    private LocalDateTime calculatedAt;
    private String calculatedBy;
    private Long settlementCycleId;
}
