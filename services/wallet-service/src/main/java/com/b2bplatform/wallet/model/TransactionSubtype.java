package com.b2bplatform.wallet.model;

/**
 * Transaction Subtype codes as per Unified Integration Standard v1.1
 * Industry standard codes for transaction categorization
 */
public enum TransactionSubtype {
    BET(300, "Bet"),
    CANCEL(301, "Cancel"),
    WIN(302, "Win"),
    LOSS(303, "Loss"),
    REFUND(304, "Refund"),
    JACKPOT(305, "Jackpot"),
    BONUS(306, "Bonus"),
    RAKE_FEE(307, "Rake/Fee");
    
    private final int code;
    private final String description;
    
    TransactionSubtype(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get TransactionSubtype by code
     */
    public static TransactionSubtype fromCode(int code) {
        for (TransactionSubtype subtype : values()) {
            if (subtype.code == code) {
                return subtype;
            }
        }
        throw new IllegalArgumentException("Invalid transaction subtype code: " + code);
    }
    
    /**
     * Check if code is valid
     */
    public static boolean isValidCode(int code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
