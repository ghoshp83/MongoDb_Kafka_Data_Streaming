package com.enterprise.department.core.process;

import com.enterprise.department.api.DocumentProcessingStrategy;
import com.enterprise.department.core.metrics.MetricsCollector;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes MongoDB documents using a strategy.
 */
public class DocumentProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    
    private final DocumentProcessingStrategy processingStrategy;
    private final MetricsCollector metricsCollector;
    
    /**
     * Creates a new DocumentProcessor.
     *
     * @param processingStrategy The strategy to use for processing documents
     * @param metricsCollector The metrics collector
     */
    public DocumentProcessor(DocumentProcessingStrategy processingStrategy, MetricsCollector metricsCollector) {
        this.processingStrategy = processingStrategy;
        this.metricsCollector = metricsCollector;
    }
    
    /**
     * Processes a document.
     *
     * @param document The document to process
     * @param operation The operation type (read, insert, update, replace, delete)
     * @param source The source of the event (initial_load or change_stream)
     */
    public void processDocument(Document document, String operation, String source) {
        try {
            MetricsCollector.Timer timer = metricsCollector.startTimer("document.process.duration");
            
            processingStrategy.processDocument(document, operation, source);
            
            timer.stop();
            metricsCollector.incrementCounter("document.processed");
        } catch (Exception e) {
            logger.error("Error processing document: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("document.errors");
        }
    }
}
