package com.enterprise.department.core.kafka;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.resilience.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Factory for creating Kafka producers.
 * Provides optimized Kafka producer configurations.
 */
@Slf4j
public class KafkaFactory {
    
    /**
     * Create a new Kafka producer with optimized settings.
     * 
     * @param config The application configuration
     * @return A new Kafka producer
     */
    public static KafkaProducer<String, String> createProducer(Config config) {
        log.warn("Creating Kafka producer with bootstrap servers: {}", config.getKafkaBootstrapServers());
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getKafkaClientId());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, config.getKafkaMaxRequestSize());
        props.put(ProducerConfig.ACKS_CONFIG, config.getKafkaAcks());
        
        // Add retry and idempotence settings for reliability
        props.put(ProducerConfig.RETRIES_CONFIG, config.getRetryMaxAttempts());
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, config.getRetryBackoffMs());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Add performance optimizations
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5); // Wait a bit to batch messages
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Default is 16KB
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // Default is 32MB
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Use compression
        
        return new KafkaProducer<>(props);
    }
    
    /**
     * Create a new Kafka producer with circuit breaker protection.
     * 
     * @param config The application configuration
     * @param circuitBreaker The circuit breaker to use
     * @return A new Kafka producer
     */
    public static KafkaProducer<String, String> createProducerWithCircuitBreaker(
            Config config, CircuitBreaker circuitBreaker) throws Exception {
        return circuitBreaker.execute(() -> createProducer(config));
    }
}
