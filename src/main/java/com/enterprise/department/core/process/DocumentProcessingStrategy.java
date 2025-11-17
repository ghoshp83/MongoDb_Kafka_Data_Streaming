package com.enterprise.department.core.process;

import org.bson.Document;

/**
 * Strategy interface for document processing.
 */
public interface DocumentProcessingStrategy extends AutoCloseable {
    
    /**
     * Processes a MongoDB document.
     *
     * @param document The MongoDB document
     * @param operation The operation type (read, insert, update, replace, delete)
     * @param source The source of the event (initial_load or change_stream)
     */
    void processDocument(Document document, String operation, String source);
    
    /**
     * Closes the strategy and releases resources.
     */
    @Override
    default void close() {
        // Default implementation does nothing
    }
}
