package com.b2bplatform.bet.exception;

public class BetException extends RuntimeException {
    public BetException(String message) {
        super(message);
    }
    
    public BetException(String message, Throwable cause) {
        super(message, cause);
    }
}
