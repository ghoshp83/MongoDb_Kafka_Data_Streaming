package com.enterprise.department.core.kafka;

import com.enterprise.department.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Factory for creating Kafka producers.
 */
public class KafkaProducerFactory {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerFactory.class);
    
    /**
     * Creates a new Kafka producer with optimized settings.
     *
     * @param config The application configuration
     * @return A new Kafka producer
     */
    public static KafkaProducer<String, String> createProducer(Config config) {
        logger.info("Creating Kafka producer for bootstrap servers: {}", config.getKafkaBootstrapServers());
        
        Properties props = new Properties();
        
        // Basic configuration
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getKafkaClientId());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Performance optimizations
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0); // Don't wait to batch messages for debugging
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1); // Small batch size for debugging
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none"); // No compression for debugging
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, config.getKafkaMaxRequestSize());
        
        // Reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, config.getKafkaAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, config.getRetryMaxAttempts());
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, config.getRetryBackoffMs());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new KafkaProducer<>(props);
    }
}
