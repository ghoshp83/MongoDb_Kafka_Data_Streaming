package com.enterprise.department.config;

/**
 * Application configuration interface.
 */
public interface Config {
    
    /**
     * Gets the MongoDB URI.
     *
     * @return The MongoDB URI
     */
    String getMongoUri();
    
    /**
     * Gets the MongoDB database name.
     *
     * @return The MongoDB database name
     */
    String getMongoDatabase();
    
    /**
     * Gets the MongoDB collection name.
     *
     * @return The MongoDB collection name
     */
    String getMongoCollection();
    
    /**
     * Gets the MongoDB batch size.
     *
     * @return The MongoDB batch size
     */
    int getMongoBatchSize();
    
    /**
     * Gets the MongoDB max pool size.
     *
     * @return The MongoDB max pool size
     */
    int getMongoMaxPoolSize();
    
    /**
     * Gets the MongoDB min pool size.
     *
     * @return The MongoDB min pool size
     */
    int getMongoMinPoolSize();
    
    /**
     * Gets the resume token path.
     *
     * @return The resume token path
     */
    String getResumeTokenPath();
    
    /**
     * Gets the resume token bucket.
     *
     * @return The resume token bucket
     */
    String getResumeTokenBucket();
    
    /**
     * Gets the resume token key.
     *
     * @return The resume token key
     */
    String getResumeTokenKey();
    
    /**
     * Gets the Kafka bootstrap servers.
     *
     * @return The Kafka bootstrap servers
     */
    String getKafkaBootstrapServers();
    
    /**
     * Gets the Kafka topic.
     *
     * @return The Kafka topic
     */
    String getKafkaTopic();
    
    /**
     * Gets the Kafka client ID.
     *
     * @return The Kafka client ID
     */
    String getKafkaClientId();
    
    /**
     * Gets the Kafka max request size.
     *
     * @return The Kafka max request size
     */
    int getKafkaMaxRequestSize();
    
    /**
     * Gets the Kafka acks.
     *
     * @return The Kafka acks
     */
    String getKafkaAcks();
    
    /**
     * Gets the Kafka batch size.
     *
     * @return The Kafka batch size
     */
    int getKafkaBatchSize();
    
    /**
     * Checks if initial load is enabled.
     *
     * @return True if initial load is enabled, false otherwise
     */
    boolean isInitialLoadEnabled();
    
    /**
     * Checks if initial load should be forced.
     *
     * @return True if initial load should be forced, false otherwise
     */
    boolean isInitialLoadForce();
    
    /**
     * Gets the health port.
     *
     * @return The health port
     */
    int getHealthPort();
    
    /**
     * Gets the retry max attempts.
     *
     * @return The retry max attempts
     */
    int getRetryMaxAttempts();
    
    /**
     * Gets the retry backoff ms.
     *
     * @return The retry backoff ms
     */
    int getRetryBackoffMs();
    
    /**
     * Gets the AWS region.
     *
     * @return The AWS region
     */
    String getAwsRegion();
}
