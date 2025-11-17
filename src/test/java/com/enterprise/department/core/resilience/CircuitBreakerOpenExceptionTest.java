package com.enterprise.department.core.resilience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerOpenExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        // Arrange
        String message = "Circuit breaker is open";
        
        // Act
        CircuitBreakerOpenException exception = new CircuitBreakerOpenException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
    }
    
    @Test
    void constructor_withMessageAndCause_shouldSetMessageAndCause() {
        // Arrange
        String message = "Circuit breaker is open";
        Throwable cause = new RuntimeException("Original error");
        
        // Act
        CircuitBreakerOpenException exception = new CircuitBreakerOpenException(message, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
