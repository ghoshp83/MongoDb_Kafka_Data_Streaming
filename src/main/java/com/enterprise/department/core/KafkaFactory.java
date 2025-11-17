package com.enterprise.department.core;

import com.enterprise.department.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Factory for creating Kafka producers.
 */
@Slf4j
public class KafkaFactory {
    
    /**
     * Create a new Kafka producer.
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
        
        // Add retry and idempotence settings
        props.put(ProducerConfig.RETRIES_CONFIG, config.getRetryMaxAttempts());
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, config.getRetryBackoffMs());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new KafkaProducer<>(props);
    }
}
