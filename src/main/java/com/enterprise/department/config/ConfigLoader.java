package com.enterprise.department.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads configuration from various sources.
 */
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    
    /**
     * Loads configuration from various sources in order of priority:
     * 1. Custom properties file specified as a command-line argument
     * 2. application.properties file in the current directory
     * 3. application.properties file in the classpath resources
     * 4. Environment variables
     *
     * @param args Command-line arguments
     * @return The loaded configuration
     * @throws IOException If an error occurs while loading configuration
     */
    public static Config loadConfig(String[] args) throws IOException {
        Properties properties = new Properties();
        
        // Load from classpath resources
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded configuration from classpath resources");
            }
        }
        
        // Load from current directory
        Path currentDirConfig = Paths.get("application.properties");
        if (Files.exists(currentDirConfig)) {
            try (InputStream input = new FileInputStream(currentDirConfig.toFile())) {
                properties.load(input);
                logger.info("Loaded configuration from current directory");
            }
        }
        
        // Load from command-line argument
        if (args.length > 0) {
            Path customConfig = Paths.get(args[0]);
            if (Files.exists(customConfig)) {
                try (InputStream input = new FileInputStream(customConfig.toFile())) {
                    properties.load(input);
                    logger.info("Loaded configuration from {}", customConfig);
                }
            } else {
                logger.warn("Configuration file not found: {}", customConfig);
            }
        }
        
        // Create and populate config object
        AppConfig config = new AppConfig();
        
        // MongoDB Configuration
        config.setMongoUri(getProperty(properties, "mongodb.uri", System.getenv("MONGODB_URI"), config.getMongoUri()));
        config.setMongoDatabase(getProperty(properties, "mongodb.database", System.getenv("MONGODB_DATABASE"), config.getMongoDatabase()));
        config.setMongoCollection(getProperty(properties, "mongodb.collection", System.getenv("MONGODB_COLLECTION"), config.getMongoCollection()));
        config.setMongoBatchSize(getIntProperty(properties, "mongodb.batch.size", System.getenv("MONGODB_BATCH_SIZE"), config.getMongoBatchSize()));
        config.setMongoMaxPoolSize(getIntProperty(properties, "mongodb.max.pool.size", System.getenv("MONGODB_MAX_POOL_SIZE"), config.getMongoMaxPoolSize()));
        config.setMongoMinPoolSize(getIntProperty(properties, "mongodb.min.pool.size", System.getenv("MONGODB_MIN_POOL_SIZE"), config.getMongoMinPoolSize()));
        
        // Resume Token Configuration
        config.setResumeTokenPath(getProperty(properties, "resume.token.path", System.getenv("RESUME_TOKEN_PATH"), config.getResumeTokenPath()));
        config.setResumeTokenBucket(getProperty(properties, "resume.token.bucket", System.getenv("RESUME_TOKEN_BUCKET"), config.getResumeTokenBucket()));
        config.setResumeTokenKey(getProperty(properties, "resume.token.key", System.getenv("RESUME_TOKEN_KEY"), config.getResumeTokenKey()));
        
        // Kafka Configuration
        config.setKafkaBootstrapServers(getProperty(properties, "kafka.bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"), config.getKafkaBootstrapServers()));
        config.setKafkaTopic(getProperty(properties, "kafka.topic", System.getenv("KAFKA_TOPIC"), config.getKafkaTopic()));
        config.setKafkaClientId(getProperty(properties, "kafka.client.id", System.getenv("KAFKA_CLIENT_ID"), config.getKafkaClientId()));
        config.setKafkaMaxRequestSize(getIntProperty(properties, "kafka.max.request.size", System.getenv("KAFKA_MAX_REQUEST_SIZE"), config.getKafkaMaxRequestSize()));
        config.setKafkaAcks(getProperty(properties, "kafka.acks", System.getenv("KAFKA_ACKS"), config.getKafkaAcks()));
        config.setKafkaBatchSize(getIntProperty(properties, "kafka.batch.size", System.getenv("KAFKA_BATCH_SIZE"), config.getKafkaBatchSize()));
        
        // Application Configuration
        config.setInitialLoadEnabled(getBooleanProperty(properties, "app.initial.load.enabled", System.getenv("INITIAL_LOAD_ENABLED"), config.isInitialLoadEnabled()));
        config.setInitialLoadForce(getBooleanProperty(properties, "app.initial.load.force", System.getenv("INITIAL_LOAD_FORCE"), config.isInitialLoadForce()));
        config.setHealthPort(getIntProperty(properties, "app.health.port", System.getenv("HEALTH_PORT"), config.getHealthPort()));
        config.setRetryMaxAttempts(getIntProperty(properties, "app.retry.max.attempts", System.getenv("RETRY_MAX_ATTEMPTS"), config.getRetryMaxAttempts()));
        config.setRetryBackoffMs(getIntProperty(properties, "app.retry.backoff.ms", System.getenv("RETRY_BACKOFF_MS"), config.getRetryBackoffMs()));
        config.setAwsRegion(getProperty(properties, "app.aws.region", System.getenv("AWS_REGION"), config.getAwsRegion()));
        
        return config;
    }
    
    private static String getProperty(Properties properties, String key, String envValue, String defaultValue) {
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return properties.getProperty(key, defaultValue);
    }
    
    private static int getIntProperty(Properties properties, String key, String envValue, int defaultValue) {
        if (envValue != null && !envValue.isEmpty()) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for {}: {}", key, envValue);
            }
        }
        
        String value = properties.getProperty(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for {}: {}", key, value);
            }
        }
        
        return defaultValue;
    }
    
    private static boolean getBooleanProperty(Properties properties, String key, String envValue, boolean defaultValue) {
        if (envValue != null && !envValue.isEmpty()) {
            return Boolean.parseBoolean(envValue);
        }
        
        String value = properties.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return Boolean.parseBoolean(value);
        }
        
        return defaultValue;
    }
}
