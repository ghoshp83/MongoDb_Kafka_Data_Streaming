package com.enterprise.department.core.resilience;

/**
 * Exception thrown when a circuit breaker is open.
 */
public class CircuitBreakerOpenException extends RuntimeException {
    
    /**
     * Creates a new CircuitBreakerOpenException with the specified message.
     *
     * @param message The error message
     */
    public CircuitBreakerOpenException(String message) {
        super(message);
    }
    
    /**
     * Creates a new CircuitBreakerOpenException with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public CircuitBreakerOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
