package com.enterprise.department.core.shutdown;

import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.core.resilience.CircuitBreaker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages graceful shutdown of application components.
 */
public class GracefulShutdownManager {
    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownManager.class);
    
    private final KafkaProducer<String, String> kafkaProducer;
    private final MetricsCollector metricsCollector;
    private final List<CircuitBreaker.RunnableWithException> shutdownHooks;
    private final List<Runnable> standardShutdownHooks;
    private final List<CompletableFuture<Void>> futures;
    
    /**
     * Creates a new GracefulShutdownManager.
     *
     * @param kafkaProducer The Kafka producer
     * @param metricsCollector The metrics collector
     */
    public GracefulShutdownManager(KafkaProducer<String, String> kafkaProducer, MetricsCollector metricsCollector) {
        this.kafkaProducer = kafkaProducer;
        this.metricsCollector = metricsCollector;
        this.shutdownHooks = new ArrayList<>();
        this.standardShutdownHooks = new ArrayList<>();
        this.futures = new ArrayList<>();
    }
    
    /**
     * Registers a shutdown hook.
     *
     * @param hook The shutdown hook to register
     */
    public void registerShutdownHook(CircuitBreaker.RunnableWithException hook) {
        shutdownHooks.add(hook);
    }
    
    /**
     * Registers a standard shutdown hook.
     *
     * @param hook The shutdown hook to register
     */
    public void registerShutdownHook(Runnable hook) {
        standardShutdownHooks.add(hook);
    }
    
    /**
     * Registers a task to be completed during shutdown.
     *
     * @param task The task to register
     */
    public void registerTask(CompletableFuture<Void> task) {
        futures.add(task);
    }
    
    /**
     * Initiates a graceful shutdown.
     */
    public void initiateShutdown() {
        logger.info("Initiating graceful shutdown");
        MetricsCollector.Timer timer = metricsCollector.startTimer("shutdown.duration");
        
        // Execute shutdown hooks
        for (CircuitBreaker.RunnableWithException hook : shutdownHooks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    hook.run();
                } catch (Exception e) {
                    logger.error("Error executing shutdown hook: {}", e.getMessage(), e);
                }
            });
            futures.add(future);
        }
        
        // Execute standard shutdown hooks
        for (Runnable hook : standardShutdownHooks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    hook.run();
                } catch (Exception e) {
                    logger.error("Error executing standard shutdown hook: {}", e.getMessage(), e);
                }
            });
            futures.add(future);
        }
        
        // Wait for shutdown hooks to complete
        boolean completed = waitForCompletion(30);
        
        // Close Kafka producer
        if (kafkaProducer != null) {
            try {
                kafkaProducer.flush();
                kafkaProducer.close();
                logger.info("Kafka producer closed");
            } catch (Exception e) {
                logger.error("Error closing Kafka producer: {}", e.getMessage(), e);
            }
        }
        
        timer.stop();
        logger.info("Graceful shutdown completed in {} ms (all tasks completed: {})",
                timer.getDurationMs(), completed);
    }
    
    /**
     * Waits for all shutdown tasks to complete.
     *
     * @param timeoutSeconds The timeout in seconds
     * @return True if all tasks completed, false if the timeout was reached
     */
    public boolean waitForCompletion(long timeoutSeconds) {
        if (futures.isEmpty()) {
            return true;
        }
        
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        
        try {
            allTasks.get(timeoutSeconds, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            logger.warn("Timeout waiting for shutdown tasks to complete: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Waits for graceful shutdown to complete.
     *
     * @param timeoutSeconds The timeout in seconds
     * @return True if shutdown completed, false if the timeout was reached
     */
    public boolean waitForGracefulShutdown(long timeoutSeconds) {
        return waitForCompletion(timeoutSeconds);
    }
}
