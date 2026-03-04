package com.b2bplatform.wallet.model;

/**
 * Transaction types for wallet transactions.
 * Based on B2C platform reference implementation.
 */
public enum TransactionType {
    /**
     * Credit Transactions (Increase Balance)
     */
    DEPOSIT,        // Player adds funds from bank/payment gateway
    WIN,            // Player wins bet (initiated by gaming provider)
    REFUND,         // Refund failed transaction (initiated by gaming provider)
    BONUS,          // Bonus credit (initiated by gaming provider or B2C)
    ADJUSTMENT,     // Manual adjustment (credit/debit based on amount sign)
    TRANSFER_IN,    // Transfer funds from operator to B2B wallet (Fund Transfer model)
    
    /**
     * Debit Transactions (Decrease Balance)
     */
    WITHDRAWAL,     // Player withdraws funds to bank/payment gateway
    BET,            // Player places bet (initiated by gaming provider)
    TRANSFER_OUT,   // Transfer funds from B2B wallet to operator (Fund Transfer model)
    
    /**
     * Query Transactions (No Balance Change)
     */
    BALANCE_QUERY;  // Balance query (no balance change)
    
    /**
     * Check if transaction type is a credit (increases balance)
     */
    public boolean isCredit() {
        return this == DEPOSIT || this == WIN || this == REFUND || 
               this == BONUS || this == TRANSFER_IN || 
               (this == ADJUSTMENT); // ADJUSTMENT can be credit or debit based on amount
    }
    
    /**
     * Check if transaction type is a debit (decreases balance)
     */
    public boolean isDebit() {
        return this == WITHDRAWAL || this == BET || this == TRANSFER_OUT ||
               (this == ADJUSTMENT); // ADJUSTMENT can be credit or debit based on amount
    }
    
    /**
     * Check if transaction type affects balance
     */
    public boolean affectsBalance() {
        return this != BALANCE_QUERY;
    }
}
