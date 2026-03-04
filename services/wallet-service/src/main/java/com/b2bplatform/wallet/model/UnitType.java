package com.b2bplatform.wallet.model;

/**
 * Unit type for transaction amounts
 * Determines how the amount field should be interpreted
 */
public enum UnitType {
    /**
     * Amount is in cents (smallest currency unit)
     * Example: 10000 cents = $100.00 USD
     */
    CENTS,
    
    /**
     * Amount is in decimal format (standard currency units)
     * Example: 100.00 = $100.00 USD
     */
    DECIMAL;
    
    /**
     * Convert amount from cents to decimal
     */
    public static java.math.BigDecimal centsToDecimal(java.math.BigDecimal cents) {
        if (cents == null) {
            return null;
        }
        return cents.divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Convert amount from decimal to cents
     */
    public static java.math.BigDecimal decimalToCents(java.math.BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        return decimal.multiply(java.math.BigDecimal.valueOf(100));
    }
}
