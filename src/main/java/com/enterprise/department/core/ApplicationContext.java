package com.enterprise.department.core;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.kafka.BatchKafkaProducer;
import com.enterprise.department.core.kafka.KafkaProducerFactory;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.core.mongo.ChangeStreamProcessor;
import com.enterprise.department.core.mongo.InitialLoader;
import com.enterprise.department.core.mongo.MongoConnectionPool;
import com.enterprise.department.core.mongo.MongoFactory;
import com.enterprise.department.core.mongo.ResumeTokenManager;
import com.enterprise.department.core.mongo.ResumeTokenManagerFactory;
import com.enterprise.department.core.process.DocumentProcessingStrategy;
import com.enterprise.department.core.process.KafkaDocumentProcessor;
import com.enterprise.department.core.resilience.CircuitBreaker;
import com.enterprise.department.core.shutdown.GracefulShutdownManager;
import com.enterprise.department.health.HealthCheckService;
import com.mongodb.client.MongoClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central component that manages all dependencies and their lifecycle.
 */
public class ApplicationContext implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);
    
    private final Config config;
    private final MongoClient mongoClient;
    private final KafkaProducer<String, String> kafkaProducer;
    private final BatchKafkaProducer batchKafkaProducer;
    private final MetricsCollector metricsCollector;
    private final CircuitBreaker mongoCircuitBreaker;
    private final CircuitBreaker kafkaCircuitBreaker;
    private final ResumeTokenManager resumeTokenManager;
    private final DocumentProcessingStrategy documentProcessingStrategy;
    private final InitialLoader initialLoader;
    private final ChangeStreamProcessor changeStreamProcessor;
    private final GracefulShutdownManager shutdownManager;
    private final HealthCheckService healthCheckService;
    
    /**
     * Creates a new ApplicationContext.
     *
     * @param config The application configuration
     */
    public ApplicationContext(Config config) {
        this.config = config;
        
        // Create core components
        this.metricsCollector = new MetricsCollector();
        this.mongoCircuitBreaker = new CircuitBreaker("mongodb", 3, 30000, metricsCollector);
        this.kafkaCircuitBreaker = new CircuitBreaker("kafka", 3, 30000, metricsCollector);
        
        // Create clients
        this.mongoClient = MongoFactory.createClient(config);
        this.kafkaProducer = KafkaProducerFactory.createProducer(config);
        this.batchKafkaProducer = new BatchKafkaProducer(kafkaProducer, config, metricsCollector);
        
        // Create processing components
        this.resumeTokenManager = ResumeTokenManagerFactory.createResumeTokenManager(config, metricsCollector);
        this.documentProcessingStrategy = new KafkaDocumentProcessor(batchKafkaProducer, metricsCollector);
        
        // Create main components
        this.initialLoader = new InitialLoader(mongoClient, config, metricsCollector, documentProcessingStrategy);
        this.changeStreamProcessor = new ChangeStreamProcessor(
                mongoClient, config, documentProcessingStrategy, metricsCollector, 
                mongoCircuitBreaker, resumeTokenManager);
        
        // Create management components
        this.shutdownManager = new GracefulShutdownManager(kafkaProducer, metricsCollector);
        this.healthCheckService = new HealthCheckService(config, mongoClient, kafkaProducer, metricsCollector);
        
        // Register shutdown hooks
        shutdownManager.registerShutdownHook((CircuitBreaker.RunnableWithException)() -> changeStreamProcessor.stop());
        shutdownManager.registerShutdownHook((CircuitBreaker.RunnableWithException)() -> batchKafkaProducer.close());
        shutdownManager.registerShutdownHook((CircuitBreaker.RunnableWithException)() -> MongoConnectionPool.closeClient());
        
        logger.info("Application context initialized");
    }
    
    /**
     * Gets the application configuration.
     *
     * @return The application configuration
     */
    public Config getConfig() {
        return config;
    }
    
    /**
     * Gets the initial loader.
     *
     * @return The initial loader
     */
    public InitialLoader getInitialLoader() {
        return initialLoader;
    }
    
    /**
     * Gets the change stream processor.
     *
     * @return The change stream processor
     */
    public ChangeStreamProcessor getChangeStreamProcessor() {
        return changeStreamProcessor;
    }
    
    /**
     * Gets the health check service.
     *
     * @return The health check service
     */
    public HealthCheckService getHealthCheckService() {
        return healthCheckService;
    }
    
    /**
     * Gets the metrics collector.
     *
     * @return The metrics collector
     */
    public MetricsCollector getMetricsCollector() {
        return metricsCollector;
    }
    
    /**
     * Gets the graceful shutdown manager.
     *
     * @return The graceful shutdown manager
     */
    public GracefulShutdownManager getShutdownManager() {
        return shutdownManager;
    }
    
    @Override
    public void close() {
        logger.info("Closing application context");
        shutdownManager.initiateShutdown();
    }
}
