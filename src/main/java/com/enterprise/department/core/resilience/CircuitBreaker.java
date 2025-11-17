package com.enterprise.department.core.resilience;

import com.enterprise.department.core.metrics.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Circuit breaker implementation to prevent cascading failures.
 */
public class CircuitBreaker {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);
    
    private enum State {
        CLOSED, OPEN, HALF_OPEN
    }
    
    private final String name;
    private final int failureThreshold;
    private final long resetTimeoutMs;
    private final MetricsCollector metricsCollector;
    
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private volatile State state = State.CLOSED;
    
    /**
     * Creates a new CircuitBreaker.
     *
     * @param name The name of the circuit breaker
     * @param failureThreshold The number of failures before opening the circuit
     * @param resetTimeoutMs The time in milliseconds before attempting to reset the circuit
     * @param metricsCollector The metrics collector
     */
    public CircuitBreaker(String name, int failureThreshold, long resetTimeoutMs, MetricsCollector metricsCollector) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
        this.metricsCollector = metricsCollector;
        
        logger.info("Created circuit breaker '{}' with failure threshold {} and reset timeout {} ms",
                name, failureThreshold, resetTimeoutMs);
    }
    
    /**
     * Executes an action with circuit breaker protection.
     *
     * @param action The action to execute
     * @param <T> The return type of the action
     * @return The result of the action
     * @throws Exception If the action throws an exception or the circuit is open
     */
    public <T> T execute(Supplier<T> action) throws Exception {
        if (isOpen()) {
            if (shouldAttemptReset()) {
                return attemptReset(action);
            }
            metricsCollector.incrementCounter("circuit." + name + ".rejected");
            throw new CircuitBreakerOpenException("Circuit breaker '" + name + "' is open");
        }
        
        try {
            T result = action.get();
            recordSuccess();
            return result;
        } catch (RuntimeException e) {
            recordFailure();
            throw e;
        }
    }
    
    /**
     * Executes a runnable with circuit breaker protection.
     *
     * @param action The action to execute
     * @throws Exception If the action throws an exception or the circuit is open
     */
    public void executeRunnable(RunnableWithException action) throws Exception {
        execute(() -> {
            try {
                action.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }
    
    /**
     * Checks if the circuit is open.
     *
     * @return True if the circuit is open, false otherwise
     */
    public boolean isOpen() {
        return state == State.OPEN;
    }
    
    /**
     * Gets the current state of the circuit breaker.
     *
     * @return The current state
     */
    public String getState() {
        return state.name();
    }
    
    private void recordSuccess() {
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            failureCount.set(0);
            logger.info("Circuit breaker '{}' reset to CLOSED state", name);
            metricsCollector.incrementCounter("circuit." + name + ".reset");
        }
    }
    
    private void recordFailure() {
        lastFailureTime.set(System.currentTimeMillis());
        
        if (state == State.HALF_OPEN) {
            state = State.OPEN;
            logger.info("Circuit breaker '{}' returned to OPEN state", name);
            metricsCollector.incrementCounter("circuit." + name + ".reopened");
        } else if (state == State.CLOSED) {
            int failures = failureCount.incrementAndGet();
            if (failures >= failureThreshold) {
                state = State.OPEN;
                logger.info("Circuit breaker '{}' transitioned to OPEN state after {} failures",
                        name, failures);
                metricsCollector.incrementCounter("circuit." + name + ".opened");
            }
        }
    }
    
    private boolean shouldAttemptReset() {
        long lastFailure = lastFailureTime.get();
        return System.currentTimeMillis() - lastFailure >= resetTimeoutMs;
    }
    
    private <T> T attemptReset(Supplier<T> action) throws Exception {
        logger.info("Circuit breaker '{}' attempting reset", name);
        state = State.HALF_OPEN;
        metricsCollector.incrementCounter("circuit." + name + ".half_open");
        
        try {
            T result = action.get();
            recordSuccess();
            return result;
        } catch (Exception e) {
            recordFailure();
            throw e;
        }
    }
    
    /**
     * Functional interface for runnables that can throw exceptions.
     */
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
