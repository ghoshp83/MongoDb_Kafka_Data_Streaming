package com.enterprise.department.core.process;

import com.enterprise.department.core.kafka.BatchKafkaProducer;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes MongoDB documents and sends them to Kafka.
 */
public class KafkaDocumentProcessor implements DocumentProcessingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(KafkaDocumentProcessor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
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
        try {
            // Extract document ID for Kafka record key
            String documentId = extractDocumentId(document);
            
            // Convert document to JSON and add metadata
            String documentJson = convertToJsonWithMetadata(document, operation, source);
            
            logger.info("Processing document: id={}, operation={}, source={}", documentId, operation, source);
            
            // Send to Kafka
            kafkaProducer.send(documentId, documentJson);
            
            // Update metrics
            metricsCollector.incrementCounter("documents.processed");
            metricsCollector.incrementCounter("documents." + operation);
            metricsCollector.incrementCounter("documents." + source);
            
        } catch (Exception e) {
            logger.error("Error processing document: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("documents.errors");
        }
    }
    
    /**
     * Extracts the document ID for use as the Kafka record key.
     * Uses the 'vuid' field if present, otherwise uses the '_id' field.
     *
     * @param document The MongoDB document
     * @return The document ID as a string
     */
    private String extractDocumentId(Document document) {
        // First check for vuid field
        if (document.containsKey("vuid")) {
            Object vuid = document.get("vuid");
            return vuid != null ? vuid.toString() : "null";
        }
        
        // Fall back to _id field
        Object id = document.get("_id");
        return id != null ? id.toString() : "null";
    }
    
    /**
     * Converts a MongoDB document to JSON and adds metadata.
     *
     * @param document The MongoDB document
     * @param operation The operation type
     * @param source The source of the event
     * @return The JSON string
     */
    private String convertToJsonWithMetadata(Document document, String operation, String source) {
        try {
            // Convert document to JSON
            String documentJson = document.toJson();
            
            // Parse JSON and add metadata
            ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(documentJson);
            jsonNode.put("_operation", operation);
            jsonNode.put("_source", source);
            jsonNode.put("_timestamp", System.currentTimeMillis());
            
            return jsonNode.toString();
        } catch (Exception e) {
            logger.error("Error converting document to JSON: {}", e.getMessage(), e);
            return "{}";
        }
    }
    
    @Override
    public void close() {
        kafkaProducer.flush();
    }
}
