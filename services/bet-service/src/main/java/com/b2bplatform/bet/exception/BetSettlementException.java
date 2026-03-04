package com.b2bplatform.bet.exception;

public class BetSettlementException extends BetException {
    public BetSettlementException(String message) {
        super(message);
    }
    
    public BetSettlementException(String message, Throwable cause) {
        super(message, cause);
    }
}
