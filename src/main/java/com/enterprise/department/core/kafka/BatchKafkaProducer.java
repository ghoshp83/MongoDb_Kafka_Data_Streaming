package com.enterprise.department.core.kafka;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimized batch processing for Kafka messages.
 */
public class BatchKafkaProducer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BatchKafkaProducer.class);
    
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final int batchSize;
    private final List<ProducerRecord<String, String>> batch;
    private final MetricsCollector metricsCollector;
    
    /**
     * Creates a new BatchKafkaProducer.
     *
     * @param producer The Kafka producer
     * @param config The application configuration
     * @param metricsCollector The metrics collector
     */
    public BatchKafkaProducer(KafkaProducer<String, String> producer, Config config, MetricsCollector metricsCollector) {
        this.producer = producer;
        this.topic = config.getKafkaTopic();
        this.batchSize = config.getKafkaBatchSize();
        this.batch = new ArrayList<>(batchSize);
        this.metricsCollector = metricsCollector;
        
        logger.info("Created batch Kafka producer with batch size: {}", batchSize);
    }
    
    /**
     * Sends a message to Kafka.
     *
     * @param key The message key
     * @param value The message value
     */
    public void send(String key, String value) {
        logger.debug("Adding message to batch: key={}", key);
        batch.add(new ProducerRecord<>(topic, key, value));
        
        if (batch.size() >= batchSize) {
            flush();
        }
    }
    
    /**
     * Flushes the batch to Kafka.
     */
    public void flush() {
        if (batch.isEmpty()) {
            return;
        }
        
        logger.info("Flushing batch of {} messages to Kafka", batch.size());
        
        try {
            for (ProducerRecord<String, String> record : batch) {
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        logger.error("Error sending message to Kafka: {}", exception.getMessage(), exception);
                        metricsCollector.incrementCounter("kafka.send.errors");
                    } else {
                        logger.info("Message sent to Kafka: topic={}, partition={}, offset={}",
                                metadata.topic(), metadata.partition(), metadata.offset());
                        metricsCollector.incrementCounter("kafka.send.success");
                    }
                });
            }
            
            producer.flush();
            metricsCollector.incrementCounter("kafka.batch.flush");
            logger.info("Batch flushed to Kafka");
        } catch (Exception e) {
            logger.error("Error flushing batch to Kafka: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("kafka.batch.errors");
        } finally {
            batch.clear();
        }
    }
    
    @Override
    public void close() {
        flush();
    }
}
