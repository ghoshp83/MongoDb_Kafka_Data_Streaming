package com.enterprise.department.api;

import org.bson.Document;

/**
 * Strategy interface for document processing.
 */
public interface DocumentProcessingStrategy {
    
    /**
     * Processes a MongoDB document.
     *
     * @param document The document to process
     * @param operation The operation type (read, insert, update, replace, delete)
     * @param source The source of the event (initial_load or change_stream)
     */
    void processDocument(Document document, String operation, String source);
}
