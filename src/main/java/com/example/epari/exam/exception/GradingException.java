package com.example.epari.exam.exception;

public class GradingException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public GradingException(String message) {
        super(message);
    }
    
    public GradingException(String message, Throwable cause) {
        super(message, cause); 
    }
}

