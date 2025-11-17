package com.enterprise.department.core;

import com.enterprise.department.api.DocumentProcessingStrategy;
import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.core.mongo.ResumeTokenManager;
import com.enterprise.department.core.resilience.CircuitBreaker;
import com.enterprise.department.core.resilience.CircuitBreakerOpenException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.client.model.changestream.OperationType;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Processes MongoDB change stream events.
 */
public class ChangeStreamProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ChangeStreamProcessor.class);
    
    private final MongoClient mongoClient;
    private final Config config;
    private final DocumentProcessingStrategy processingStrategy;
    private final MetricsCollector metricsCollector;
    private final CircuitBreaker circuitBreaker;
    private final ResumeTokenManager resumeTokenManager;
    
    private volatile boolean running = false;
    
    /**
     * Creates a new ChangeStreamProcessor.
     *
     * @param mongoClient The MongoDB client
     * @param config The application configuration
     * @param processingStrategy The strategy for processing documents
     * @param metricsCollector The metrics collector
     * @param circuitBreaker The circuit breaker for MongoDB operations
     * @param resumeTokenManager The manager for resume tokens
     */
    public ChangeStreamProcessor(
            MongoClient mongoClient,
            Config config,
            DocumentProcessingStrategy processingStrategy,
            MetricsCollector metricsCollector,
            CircuitBreaker circuitBreaker,
            ResumeTokenManager resumeTokenManager) {
        this.mongoClient = mongoClient;
        this.config = config;
        this.processingStrategy = processingStrategy;
        this.metricsCollector = metricsCollector;
        this.circuitBreaker = circuitBreaker;
        this.resumeTokenManager = resumeTokenManager;
    }
    
    /**
     * Starts processing the MongoDB change stream.
     *
     * @throws Exception If an error occurs during processing
     */
    public void startChangeStream() throws Exception {
        if (running) {
            logger.warn("Change stream processor is already running");
            return;
        }
        
        running = true;
        logger.info("Starting change stream processor");
        
        try {
            circuitBreaker.executeRunnable(() -> {
                MongoDatabase database = mongoClient.getDatabase(config.getMongoDatabase());
                MongoCollection<Document> collection = database.getCollection(config.getMongoCollection());
                
                // Load the resume token if available
                Optional<BsonDocument> resumeToken = Optional.ofNullable(resumeTokenManager.loadResumeToken());
                
                // Create the change stream
                MongoCursor<ChangeStreamDocument<Document>> cursor;
                if (resumeToken.isPresent()) {
                    logger.info("Resuming change stream from token");
                    cursor = collection.watch(createPipeline())
                            .fullDocument(FullDocument.UPDATE_LOOKUP)
                            .resumeAfter(resumeToken.get())
                            .iterator();
                } else {
                    logger.info("Starting new change stream");
                    cursor = collection.watch(createPipeline())
                            .fullDocument(FullDocument.UPDATE_LOOKUP)
                            .iterator();
                }
                
                // Process the change stream
                while (running && cursor.hasNext()) {
                    try {
                        ChangeStreamDocument<Document> event = cursor.next();
                        processChangeEvent(event);
                    } catch (Exception e) {
                        if (running) {
                            logger.error("Error processing change stream event: {}", e.getMessage(), e);
                            metricsCollector.incrementCounter("change_stream.errors");
                        }
                    }
                }
                
                cursor.close();
            });
        } catch (CircuitBreakerOpenException e) {
            logger.error("Circuit breaker is open, cannot start change stream: {}", e.getMessage());
            metricsCollector.incrementCounter("change_stream.circuit_breaker.open");
            throw e;
        } catch (Exception e) {
            logger.error("Error starting change stream: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("change_stream.start.errors");
            throw e;
        }
    }
    
    /**
     * Stops the change stream processor.
     */
    public void stop() {
        logger.info("Stopping change stream processor");
        running = false;
    }
    
    /**
     * Processes a change stream event.
     *
     * @param event The change stream event
     */
    void processChangeEvent(ChangeStreamDocument<Document> event) {
        try {
            OperationType operationType = event.getOperationType();
            String operation = sanitizeOperation(operationType.getValue());
            
            metricsCollector.incrementCounter("change_stream.events.processed");
            metricsCollector.incrementCounter("change_stream.events." + operation);
            
            // Process based on operation type
            switch (operationType) {
                case INSERT:
                case UPDATE:
                case REPLACE:
                    Document document = event.getFullDocument();
                    if (document != null) {
                        processingStrategy.processDocument(document, operation, "change_stream");
                    }
                    break;
                    
                case DELETE:
                    BsonDocument bsonDocumentKey = event.getDocumentKey();
                    if (bsonDocumentKey != null) {
                        Document documentKey = new Document(bsonDocumentKey);
                        processingStrategy.processDocument(documentKey, operation, "change_stream");
                    }
                    break;
                    
                default:
                    logger.debug("Ignoring operation type: {}", operationType);
                    break;
            }
            
            // Save the resume token
            BsonDocument resumeToken = event.getResumeToken();
            if (resumeToken != null) {
                resumeTokenManager.saveResumeToken(resumeToken);
            }
        } catch (Exception e) {
            logger.error("Error processing change event: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("change_stream.events.errors");
        }
    }
    
    private List<Bson> createPipeline() {
        // Create a pipeline that filters out system collections and operations
        return Arrays.asList(
                Aggregates.match(
                        Filters.and(
                                Filters.ne("ns.coll", "system.indexes"),
                                Filters.ne("operationType", "invalidate")
                        )
                )
        );
    }
    
    private String sanitizeOperation(String operation) {
        // Whitelist of allowed operations to prevent code injection
        switch (operation) {
            case "insert":
            case "update":
            case "replace":
            case "delete":
            case "drop":
            case "rename":
            case "dropDatabase":
            case "invalidate":
                return operation;
            default:
                return "unknown";
        }
    }
}
