package com.b2bplatform.bet.exception;

public class BetValidationException extends BetException {
    public BetValidationException(String message) {
        super(message);
    }
    
    public BetValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
