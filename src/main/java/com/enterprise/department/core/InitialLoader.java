package com.enterprise.department.core;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.util.DocumentConverter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the initial loading of data from MongoDB to Kafka.
 */
public class InitialLoader {
    private static final Logger logger = LoggerFactory.getLogger(InitialLoader.class);
    private static final String INITIAL_LOAD_MARKER_ID = "initial_load_marker";
    
    private final MongoClient mongoClient;
    private final KafkaProducer<String, String> kafkaProducer;
    private final Config config;
    private final MetricsCollector metricsCollector;
    
    /**
     * Creates a new InitialLoader.
     *
     * @param mongoClient The MongoDB client
     * @param kafkaProducer The Kafka producer
     * @param config The application configuration
     * @param metricsCollector The metrics collector
     */
    public InitialLoader(MongoClient mongoClient, KafkaProducer<String, String> kafkaProducer, 
                         Config config, MetricsCollector metricsCollector) {
        this.mongoClient = mongoClient;
        this.kafkaProducer = kafkaProducer;
        this.config = config;
        this.metricsCollector = metricsCollector;
    }
    
    /**
     * Loads all documents from MongoDB to Kafka.
     * If initial load is disabled or has already been completed, this method does nothing.
     *
     * @throws Exception If an error occurs during loading
     */
    public void loadInitialData() throws Exception {
        if (!config.isInitialLoadEnabled()) {
            logger.info("Initial load is disabled, skipping");
            return;
        }
        
        MongoCollection<Document> collection = mongoClient
                .getDatabase(config.getMongoDatabase())
                .getCollection(config.getMongoCollection());
        
        // Check if initial load has already been completed
        if (!config.isInitialLoadForce() && isInitialLoadCompleted(collection)) {
            logger.info("Initial load has already been completed, skipping");
            return;
        }
        
        logger.info("Starting initial data load from MongoDB to Kafka");
        
        try {
            int batchSize = config.getMongoBatchSize();
            int processedCount = 0;
            int batchCount = 0;
            
            MetricsCollector.Timer timer = metricsCollector.startTimer("initial_load.duration");
            
            FindIterable<Document> documents = collection.find();
            documents.batchSize(batchSize);
            
            try (MongoCursor<Document> cursor = documents.iterator()) {
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    String id = DocumentConverter.extractId(document);
                    Document enrichedDoc = DocumentConverter.addMetadata(document, "read", "initial_load");
                    String json = DocumentConverter.convertToJsonString(enrichedDoc);
                    
                    kafkaProducer.send(new ProducerRecord<>(config.getKafkaTopic(), id, json));
                    processedCount++;
                    
                    // Flush after each batch
                    if (processedCount % batchSize == 0) {
                        kafkaProducer.flush();
                        batchCount++;
                        logger.info("Processed {} documents ({} batches)", processedCount, batchCount);
                        metricsCollector.incrementCounter("initial_load.documents.processed");
                    }
                }
            }
            
            // Final flush
            kafkaProducer.flush();
            timer.stop();
            
            logger.info("Initial data load completed: {} documents processed in {} ms", 
                    processedCount, timer.getDurationMs());
            
            // Mark initial load as completed
            markInitialLoadCompleted(collection);
        } catch (Exception e) {
            logger.error("Error during initial data load: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("initial_load.errors");
            throw e;
        }
    }
    
    private boolean isInitialLoadCompleted(MongoCollection<Document> collection) {
        Document marker = collection.find(Filters.eq("_id", INITIAL_LOAD_MARKER_ID)).first();
        return marker != null && Boolean.TRUE.equals(marker.getBoolean("completed", false));
    }
    
    private void markInitialLoadCompleted(MongoCollection<Document> collection) {
        Document marker = new Document("_id", INITIAL_LOAD_MARKER_ID)
                .append("completed", true)
                .append("timestamp", System.currentTimeMillis());
        
        collection.insertOne(marker);
        logger.info("Marked initial load as completed");
    }
}
