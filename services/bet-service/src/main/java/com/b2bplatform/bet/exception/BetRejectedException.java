package com.b2bplatform.bet.exception;

public class BetRejectedException extends BetException {
    public BetRejectedException(String message) {
        super(message);
    }
    
    public BetRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
