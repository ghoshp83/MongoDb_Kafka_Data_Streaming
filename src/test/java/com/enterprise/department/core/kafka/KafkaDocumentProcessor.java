package com.enterprise.department.core.kafka;

import com.enterprise.department.api.DocumentProcessingStrategy;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.util.DocumentConverter;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of DocumentProcessingStrategy that sends documents to Kafka.
 * This is a test implementation for testing purposes.
 */
public class KafkaDocumentProcessor implements DocumentProcessingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(KafkaDocumentProcessor.class);
    
    private final BatchKafkaProducer kafkaProducer;
    private final MetricsCollector metricsCollector;
    
    /**
     * Creates a new KafkaDocumentProcessor.
     *
     * @param kafkaProducer The Kafka producer
     * @param metricsCollector The metrics collector
     */
    public KafkaDocumentProcessor(BatchKafkaProducer kafkaProducer, MetricsCollector metricsCollector) {
        this.kafkaProducer = kafkaProducer;
        this.metricsCollector = metricsCollector;
    }
    
    @Override
    public void processDocument(Document document, String operation, String source) {
        if (document == null) {
            logger.error("Received null document for processing");
            metricsCollector.incrementCounter("kafka.documents.errors");
            return;
        }
        
        try {
            // Extract the document ID for use as the Kafka record key
            String id = DocumentConverter.extractId(document);
            
            // Add metadata to the document
            Document enrichedDoc = DocumentConverter.addMetadata(document, operation, source);
            
            // Convert the document to JSON
            String json = DocumentConverter.convertToJsonString(enrichedDoc);
            
            // Send the document to Kafka
            kafkaProducer.send(id, json);
            
            // Update metrics
            metricsCollector.incrementCounter("kafka.documents.processed");
            metricsCollector.incrementCounter("kafka.documents." + operation);
            
            logger.debug("Processed document: id={}, operation={}, source={}", id, operation, source);
        } catch (Exception e) {
            logger.error("Error processing document: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("kafka.documents.errors");
        }
    }
}
