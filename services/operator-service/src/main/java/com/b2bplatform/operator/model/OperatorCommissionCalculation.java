package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for commission calculation history.
 * Records commission calculations for audit and reporting.
 */
@Entity
@Table(name = "operator_commission_calculations", indexes = {
    @Index(name = "idx_commission_calc_operator_id", columnList = "operator_id"),
    @Index(name = "idx_commission_calc_provider_id", columnList = "game_provider_id"),
    @Index(name = "idx_commission_calc_period", columnList = "calculation_period_start, calculation_period_end"),
    @Index(name = "idx_commission_calc_settlement", columnList = "settlement_cycle_id")
})
@Data
public class OperatorCommissionCalculation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "game_provider_id", nullable = false, length = 100)
    private String gameProviderId;
    
    @Column(name = "game_id", length = 100)
    private String gameId;
    
    @Column(name = "commission_config_id")
    private Long commissionConfigId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "commission_model", nullable = false, length = 50)
    private CommissionModelType commissionModel;
    
    // Calculation Period
    @Column(name = "calculation_period_start", nullable = false)
    private LocalDateTime calculationPeriodStart;
    
    @Column(name = "calculation_period_end", nullable = false)
    private LocalDateTime calculationPeriodEnd;
    
    // GGR-Based Metrics
    @Column(name = "total_bets", precision = 19, scale = 2)
    private BigDecimal totalBets;
    
    @Column(name = "total_winnings", precision = 19, scale = 2)
    private BigDecimal totalWinnings;
    
    @Column(name = "ggr", precision = 19, scale = 2)
    private BigDecimal ggr; // Total Bets - Total Winnings
    
    @Column(name = "operator_commission", precision = 19, scale = 2)
    private BigDecimal operatorCommission;
    
    @Column(name = "provider_commission", precision = 19, scale = 2)
    private BigDecimal providerCommission;
    
    // Fixed Price Metrics
    @Column(name = "number_of_bets")
    private Integer numberOfBets;
    
    @Column(name = "fixed_price_per_bet", precision = 19, scale = 2)
    private BigDecimal fixedPricePerBet;
    
    @Column(name = "total_operator_fee", precision = 19, scale = 2)
    private BigDecimal totalOperatorFee;
    
    // Winnings-Based Metrics
    @Column(name = "total_winnings_amount", precision = 19, scale = 2)
    private BigDecimal totalWinningsAmount;
    
    @Column(name = "winnings_commission_rate", precision = 5, scale = 2)
    private BigDecimal winningsCommissionRate;
    
    @Column(name = "total_commission", precision = 19, scale = 2)
    private BigDecimal totalCommission;
    
    @Column(name = "operator_commission_share", precision = 19, scale = 2)
    private BigDecimal operatorCommissionShare;
    
    @Column(name = "provider_commission_share", precision = 19, scale = 2)
    private BigDecimal providerCommissionShare;
    
    // Common Fields
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
    
    @Column(name = "calculated_by", length = 100)
    private String calculatedBy; // System or admin username
    
    @Column(name = "settlement_cycle_id")
    private Long settlementCycleId; // Reference to settlement cycle
    
    @PrePersist
    protected void onCreate() {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}
