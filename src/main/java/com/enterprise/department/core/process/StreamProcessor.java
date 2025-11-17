package com.enterprise.department.core.process;

import com.enterprise.department.api.ChangeEventObserver;
import com.enterprise.department.api.DocumentProcessingStrategy;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.core.mongo.ResumeTokenManager;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import org.bson.BsonDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes MongoDB change stream events.
 */
public class StreamProcessor implements ChangeEventObserver {
    private static final Logger logger = LoggerFactory.getLogger(StreamProcessor.class);
    
    private final DocumentProcessingStrategy processingStrategy;
    private final MetricsCollector metricsCollector;
    private final ResumeTokenManager resumeTokenManager;
    
    /**
     * Creates a new StreamProcessor.
     *
     * @param processingStrategy The strategy to use for processing documents
     * @param metricsCollector The metrics collector
     * @param resumeTokenManager The manager for resume tokens
     */
    public StreamProcessor(DocumentProcessingStrategy processingStrategy, 
                          MetricsCollector metricsCollector,
                          ResumeTokenManager resumeTokenManager) {
        this.processingStrategy = processingStrategy;
        this.metricsCollector = metricsCollector;
        this.resumeTokenManager = resumeTokenManager;
    }
    
    /**
     * Creates a new StreamProcessor without metrics and resume token management.
     *
     * @param processingStrategy The strategy to use for processing documents
     */
    public StreamProcessor(DocumentProcessingStrategy processingStrategy) {
        this(processingStrategy, 
             new MetricsCollector(), 
             null);
    }
    
    /**
     * Processes a change stream event.
     *
     * @param event The change stream event
     */
    @Override
    public void onEvent(ChangeStreamDocument<Document> event) {
        try {
            OperationType operationType = event.getOperationType();
            String operation = operationType.getValue();
            
            if (metricsCollector != null) {
                metricsCollector.incrementCounter("change_stream.events.processed");
                metricsCollector.incrementCounter("change_stream.events." + operation);
            }
            
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
                        Document documentKey = Document.parse(bsonDocumentKey.toJson());
                        processingStrategy.processDocument(documentKey, operation, "change_stream");
                    }
                    break;
                    
                default:
                    logger.debug("Ignoring operation type: {}", operationType);
                    break;
            }
            
            // Save the resume token if manager is available
            if (resumeTokenManager != null) {
                BsonDocument resumeToken = event.getResumeToken();
                if (resumeToken != null) {
                    resumeTokenManager.saveResumeToken(resumeToken);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing change event: {}", e.getMessage(), e);
            if (metricsCollector != null) {
                metricsCollector.incrementCounter("change_stream.events.errors");
            }
        }
    }
}
