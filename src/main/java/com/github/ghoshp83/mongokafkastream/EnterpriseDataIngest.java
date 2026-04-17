package com.github.ghoshp83.mongokafkastream;

import com.github.ghoshp83.mongokafkastream.config.Config;
import com.github.ghoshp83.mongokafkastream.config.ConfigLoader;
import com.github.ghoshp83.mongokafkastream.core.ApplicationContext;
import com.github.ghoshp83.mongokafkastream.core.mongo.ChangeStreamProcessor;
import com.github.ghoshp83.mongokafkastream.core.mongo.InitialLoader;
import com.github.ghoshp83.mongokafkastream.health.HealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class.
 */
public class EnterpriseDataIngest {
    private static final Logger logger = LoggerFactory.getLogger(EnterpriseDataIngest.class);
    
    /**
     * Main method.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Load configuration
            Config config = ConfigLoader.loadConfig(args);
            
            // Create application context
            try (ApplicationContext context = new ApplicationContext(config)) {
                // Start health check service
                HealthCheckService healthCheckService = context.getHealthCheckService();
                
                // Perform initial load if enabled
                InitialLoader initialLoader = context.getInitialLoader();
                initialLoader.loadInitialData();
                
                // Start change stream processor
                ChangeStreamProcessor changeStreamProcessor = context.getChangeStreamProcessor();
                changeStreamProcessor.startChangeStream();
                
                // Add shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Shutdown hook triggered");
                    context.getShutdownManager().initiateShutdown();
                }));
                
                // Keep the application running
                logger.info("Application started successfully");
                Thread.currentThread().join();
            }
        } catch (Exception e) {
            logger.error("Error starting application: {}", e.getMessage(), e);
            // Don't exit in test environment
            if (!"test".equals(System.getProperty("env"))) {
                System.exit(1);
            }
        }
    }
}
