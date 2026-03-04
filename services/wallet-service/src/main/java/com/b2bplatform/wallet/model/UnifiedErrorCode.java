package com.b2bplatform.wallet.model;

/**
 * Unified Error Codes as per B2B/B2C Unified Gaming Integration Standard v1.1
 * 
 * Harmonized error codes for consistent error reporting across B2B and B2C integrations
 */
public enum UnifiedErrorCode {
    
    /**
     * Success (1000)
     * B2B Ref: 0, B2C Ref: 1
     */
    SUCCESS(1000, "Success"),
    
    /**
     * Insufficient Funds (2001)
     * B2B Ref: 107, B2C Ref: 200
     */
    INSUFFICIENT_FUNDS(2001, "Insufficient Funds"),
    
    /**
     * Account Blocked (2002)
     * B2B Ref: 105, B2C Ref: 102
     */
    ACCOUNT_BLOCKED(2002, "Account Blocked"),
    
    /**
     * Duplicate Reference (3001)
     * B2B Ref: 111, B2C Ref: N/A
     */
    DUPLICATE_REFERENCE(3001, "Duplicate Reference"),
    
    /**
     * Token Invalid (4001)
     * B2B Ref: 113, B2C Ref: 101/2000
     */
    TOKEN_INVALID(4001, "Token Invalid"),
    
    /**
     * System Error (5000)
     * B2B Ref: 999, B2C Ref: 500
     */
    SYSTEM_ERROR(5000, "System Error");
    
    private final int code;
    private final String message;
    
    UnifiedErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * Get UnifiedErrorCode by code
     */
    public static UnifiedErrorCode fromCode(int code) {
        for (UnifiedErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR; // Default to system error for unknown codes
    }
    
    /**
     * Check if code is valid
     */
    public static boolean isValidCode(int code) {
        for (UnifiedErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return true;
            }
        }
        return false;
    }
}
