package com.b2bplatform.operator.model;

/**
 * Enumeration of commission model types.
 */
public enum CommissionModelType {
    /**
     * GGR-Based Commission (%)
     * Commission calculated as percentage of Gross Gaming Revenue (GGR).
     */
    GGR_BASED,
    
    /**
     * Fixed Price per Bet
     * Fixed fee charged per bet regardless of bet value or outcome.
     */
    FIXED_PRICE_PER_BET,
    
    /**
     * Winnings-Based Commission (%)
     * Commission calculated as percentage of player winnings.
     */
    WINNINGS_BASED
}
