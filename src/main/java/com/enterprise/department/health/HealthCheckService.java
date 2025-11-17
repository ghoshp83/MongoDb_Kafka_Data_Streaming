package com.enterprise.department.health;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for checking the health of application components.
 */
public class HealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    
    private final Config config;
    private final MongoClient mongoClient;
    private final KafkaProducer<String, String> kafkaProducer;
    private final MetricsCollector metricsCollector;
    
    /**
     * Creates a new HealthCheckService.
     *
     * @param config The application configuration
     * @param mongoClient The MongoDB client
     * @param kafkaProducer The Kafka producer
     * @param metricsCollector The metrics collector
     */
    public HealthCheckService(
            Config config,
            MongoClient mongoClient,
            KafkaProducer<String, String> kafkaProducer,
            MetricsCollector metricsCollector) {
        this.config = config;
        this.mongoClient = mongoClient;
        this.kafkaProducer = kafkaProducer;
        this.metricsCollector = metricsCollector;
    }
    
    /**
     * Checks the health of all application components.
     *
     * @return A HealthStatus object containing the health status of each component
     */
    public HealthStatus checkHealth() {
        Map<String, ComponentHealth> components = new HashMap<>();
        boolean isHealthy = true;
        
        // Check MongoDB health
        ComponentHealth mongoHealth = checkMongoHealth();
        components.put("mongodb", mongoHealth);
        isHealthy &= mongoHealth.isHealthy();
        
        // Check Kafka health
        ComponentHealth kafkaHealth = checkKafkaHealth();
        components.put("kafka", kafkaHealth);
        isHealthy &= kafkaHealth.isHealthy();
        
        // Application health is based on MongoDB and Kafka health
        ComponentHealth appHealth = new ComponentHealth(isHealthy, "Application status");
        components.put("application", appHealth);
        
        // Get metrics
        String metrics = metricsCollector.getMetricsReport();
        
        return new HealthStatus(isHealthy, components, metrics);
    }
    
    /**
     * Checks if the application is ready to serve requests.
     *
     * @return True if the application is ready, false otherwise
     */
    public boolean isReady() {
        return checkHealth().isHealthy();
    }
    
    /**
     * Gets the health status of all components.
     *
     * @return A list of component health statuses
     */
    public List<ComponentHealth> getComponentHealths() {
        Map<String, ComponentHealth> components = checkHealth().getComponents();
        return List.copyOf(components.values());
    }
    
    private ComponentHealth checkMongoHealth() {
        try {
            MongoDatabase database = mongoClient.getDatabase(config.getMongoDatabase());
            database.runCommand(new Document("ping", 1));
            return new ComponentHealth("mongodb", HealthStatus.UP, "MongoDB connection is healthy");
        } catch (Exception e) {
            logger.error("MongoDB health check failed: {}", e.getMessage(), e);
            return new ComponentHealth("mongodb", HealthStatus.DOWN, "MongoDB connection failed: " + e.getMessage());
        }
    }
    
    private ComponentHealth checkKafkaHealth() {
        try {
            kafkaProducer.partitionsFor(config.getKafkaTopic());
            return new ComponentHealth("kafka", HealthStatus.UP, "Kafka connection is healthy");
        } catch (Exception e) {
            logger.error("Kafka health check failed: {}", e.getMessage(), e);
            return new ComponentHealth("kafka", HealthStatus.DOWN, "Kafka connection failed: " + e.getMessage());
        }
    }
}
