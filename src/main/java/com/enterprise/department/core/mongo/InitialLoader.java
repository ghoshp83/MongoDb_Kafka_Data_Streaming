package com.enterprise.department.core.mongo;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.core.process.DocumentProcessingStrategy;
import com.enterprise.department.util.DocumentConverter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the initial loading of data from MongoDB to Kafka.
 */
public class InitialLoader {
    private static final Logger logger = LoggerFactory.getLogger(InitialLoader.class);
    
    private final MongoClient mongoClient;
    private final Config config;
    private final MetricsCollector metricsCollector;
    private final DocumentProcessingStrategy processingStrategy;
    
    /**
     * Creates a new InitialLoader.
     *
     * @param mongoClient The MongoDB client
     * @param config The application configuration
     * @param metricsCollector The metrics collector
     * @param processingStrategy The document processing strategy
     */
    public InitialLoader(MongoClient mongoClient, Config config, 
                         MetricsCollector metricsCollector, 
                         DocumentProcessingStrategy processingStrategy) {
        this.mongoClient = mongoClient;
        this.config = config;
        this.metricsCollector = metricsCollector;
        this.processingStrategy = processingStrategy;
    }
    
    /**
     * Loads all documents from MongoDB to Kafka.
     * If initial load is disabled, this method does nothing.
     *
     * @throws Exception If an error occurs during loading
     */
    public void loadInitialData() throws Exception {
        if (!config.isInitialLoadEnabled()) {
            logger.info("Initial load is disabled, skipping");
            return;
        }
        
        logger.info("Starting initial data load from MongoDB to Kafka");
        
        try {
            int batchSize = config.getMongoBatchSize();
            int processedCount = 0;
            int batchCount = 0;
            
            MetricsCollector.Timer timer = metricsCollector.startTimer("initial_load.duration");
            
            MongoCollection<Document> collection = mongoClient
                    .getDatabase(config.getMongoDatabase())
                    .getCollection(config.getMongoCollection());
            
            FindIterable<Document> documents = collection.find();
            documents.batchSize(batchSize);
            
            try (MongoCursor<Document> cursor = documents.iterator()) {
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    processingStrategy.processDocument(document, "read", "initial_load");
                    processedCount++;
                    
                    // Log progress after each batch
                    if (processedCount % batchSize == 0) {
                        batchCount++;
                        logger.info("Processed {} documents ({} batches)", processedCount, batchCount);
                        metricsCollector.incrementCounter("initial_load.batch.processed");
                    }
                }
            }
            
            timer.stop();
            
            logger.info("Initial data load completed: {} documents processed in {} ms", 
                    processedCount, timer.getDurationMs());
            
        } catch (Exception e) {
            logger.error("Error during initial data load: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("initial_load.errors");
            throw e;
        }
    }
}
