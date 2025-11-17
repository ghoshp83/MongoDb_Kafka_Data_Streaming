package com.enterprise.department.core.resilience;

import com.enterprise.department.core.metrics.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CircuitBreakerTest {

    @Mock
    private MetricsCollector metricsCollector;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = new CircuitBreaker("test", 3, 100, metricsCollector);
    }

    @Test
    void execute_WhenNoFailures_ShouldExecuteAction() throws Exception {
        // When
        String result = circuitBreaker.execute(() -> "success");
        
        // Then
        assertEquals("success", result);
        assertFalse(circuitBreaker.isOpen());
    }

    @Test
    void execute_WhenActionThrowsException_ShouldIncrementFailureCount() {
        // Given
        AtomicInteger callCount = new AtomicInteger(0);
        
        // When/Then
        for (int i = 0; i < 3; i++) {
            try {
                circuitBreaker.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("Test exception");
                });
                fail("Should have thrown exception");
            } catch (Exception e) {
                assertEquals("Test exception", e.getMessage());
            }
        }
        
        // After 3 failures, the circuit should be open
        assertEquals(3, callCount.get());
        assertTrue(circuitBreaker.isOpen());
        
        // Further calls should throw CircuitBreakerOpenException without executing the action
        try {
            circuitBreaker.execute(() -> {
                callCount.incrementAndGet();
                return "should not be called";
            });
            fail("Should have thrown CircuitBreakerOpenException");
        } catch (Exception e) {
            assertTrue(e instanceof CircuitBreakerOpenException);
            assertEquals("Circuit breaker 'test' is open", e.getMessage());
        }
        
        // Call count should still be 3
        assertEquals(3, callCount.get());
        
        // Verify metrics were incremented
        verify(metricsCollector).incrementCounter("circuit.test.opened");
        verify(metricsCollector).incrementCounter("circuit.test.rejected");
    }

    @Test
    void executeRunnable_ShouldExecuteAction() throws Exception {
        // Given
        AtomicInteger callCount = new AtomicInteger(0);
        
        // When
        circuitBreaker.executeRunnable(() -> callCount.incrementAndGet());
        
        // Then
        assertEquals(1, callCount.get());
    }

    @Test
    void getState_ShouldReturnCurrentState() {
        // Initially closed
        assertEquals("CLOSED", circuitBreaker.getState());
        
        // After failures, should be open
        try {
            for (int i = 0; i < 3; i++) {
                try {
                    circuitBreaker.execute(() -> {
                        throw new RuntimeException("Test exception");
                    });
                } catch (Exception e) {
                    // Expected
                }
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        assertEquals("OPEN", circuitBreaker.getState());
    }
}
