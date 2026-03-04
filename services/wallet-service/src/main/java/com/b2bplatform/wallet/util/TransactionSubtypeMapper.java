package com.b2bplatform.wallet.util;

import com.b2bplatform.wallet.model.TransactionSubtype;
import com.b2bplatform.wallet.model.TransactionType;
import lombok.extern.slf4j.Slf4j;

/**
 * Transaction Subtype Mapper
 * Maps between internal TransactionType and industry-standard TransactionSubtype codes
 * 
 * Industry Standard Codes (300-307):
 * - 300: BET
 * - 301: CANCEL
 * - 302: WIN
 * - 303: LOSS
 * - 304: REFUND
 * - 305: JACKPOT
 * - 306: BONUS
 * - 307: RAKE_FEE
 */
@Slf4j
public class TransactionSubtypeMapper {
    
    /**
     * Map TransactionType to TransactionSubtype code
     * 
     * @param transactionType Internal transaction type
     * @return TransactionSubtype code (300-307)
     */
    public static Integer toSubtypeCode(TransactionType transactionType) {
        if (transactionType == null) {
            log.warn("TransactionType is null, returning null subtype code");
            return null;
        }
        
        return switch (transactionType) {
            case BET -> TransactionSubtype.BET.getCode(); // 300
            case WIN -> TransactionSubtype.WIN.getCode(); // 302
            case REFUND -> TransactionSubtype.REFUND.getCode(); // 304
            case BONUS -> TransactionSubtype.BONUS.getCode(); // 306
            case DEPOSIT -> TransactionSubtype.BET.getCode(); // 300 (deposit is like a bet from operator perspective)
            case WITHDRAWAL -> TransactionSubtype.BET.getCode(); // 300 (withdrawal is like a bet from operator perspective)
            case TRANSFER_IN -> TransactionSubtype.BET.getCode(); // 300
            case TRANSFER_OUT -> TransactionSubtype.BET.getCode(); // 300
            case ADJUSTMENT -> TransactionSubtype.BONUS.getCode(); // 306 (adjustment treated as bonus)
            case BALANCE_QUERY -> null; // No subtype for query
        };
    }
    
    /**
     * Map TransactionSubtype code to TransactionType
     * 
     * @param subtypeCode TransactionSubtype code (300-307)
     * @return TransactionType (best match)
     */
    public static TransactionType toTransactionType(Integer subtypeCode) {
        if (subtypeCode == null) {
            log.warn("Subtype code is null, returning null transaction type");
            return null;
        }
        
        try {
            TransactionSubtype subtype = TransactionSubtype.fromCode(subtypeCode);
            return switch (subtype) {
                case BET -> TransactionType.BET;
                case CANCEL -> TransactionType.BET; // Cancel is treated as BET reversal
                case WIN -> TransactionType.WIN;
                case LOSS -> TransactionType.BET; // Loss is treated as BET
                case REFUND -> TransactionType.REFUND;
                case JACKPOT -> TransactionType.WIN; // Jackpot is treated as WIN
                case BONUS -> TransactionType.BONUS;
                case RAKE_FEE -> TransactionType.BET; // Rake fee is treated as BET
            };
        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction subtype code: {}", subtypeCode, e);
            return null;
        }
    }
    
    /**
     * Get TransactionSubtype enum from code
     * 
     * @param subtypeCode TransactionSubtype code (300-307)
     * @return TransactionSubtype enum
     */
    public static TransactionSubtype toSubtype(Integer subtypeCode) {
        if (subtypeCode == null) {
            return null;
        }
        
        try {
            return TransactionSubtype.fromCode(subtypeCode);
        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction subtype code: {}", subtypeCode, e);
            return null;
        }
    }
    
    /**
     * Validate transaction subtype code
     * 
     * @param subtypeCode TransactionSubtype code (300-307)
     * @return true if valid, false otherwise
     */
    public static boolean isValidSubtypeCode(Integer subtypeCode) {
        return TransactionSubtype.isValidCode(subtypeCode);
    }
    
    /**
     * Get default subtype code for a transaction type
     * Used when subtype is not provided in B2B request
     * 
     * @param transactionType Internal transaction type
     * @return Default TransactionSubtype code
     */
    public static Integer getDefaultSubtypeCode(TransactionType transactionType) {
        return toSubtypeCode(transactionType);
    }
}
