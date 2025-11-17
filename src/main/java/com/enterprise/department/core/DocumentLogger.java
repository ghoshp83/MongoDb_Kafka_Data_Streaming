package com.enterprise.department.core;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

/**
 * Utility class for logging MongoDB documents.
 */
@Slf4j
public class DocumentLogger {
    
    private DocumentLogger() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Log a MongoDB document with operation and source information.
     *
     * @param document The MongoDB document
     * @param operation The operation type (read, insert, update, replace, delete)
     * @param source The source of the event (initial_load or change_stream)
     */
    public static void logDocument(Document document, String operation, String source) {
        if (document == null) {
            log.debug("Null document for operation: {}, source: {}", operation, source);
            return;
        }
        
        Object id = document.get("_id");
        String idStr = id != null ? id.toString() : "null";
        
        if (log.isDebugEnabled()) {
            log.debug("Processing document: id={}, operation={}, source={}", idStr, operation, source);
        }
    }
    
    /**
     * Log a document ID with operation and source information.
     *
     * @param documentId The document ID
     * @param source The source of the event (initial_load or change_stream)
     */
    public static void logDocumentId(String documentId, String source) {
        if (log.isDebugEnabled()) {
            log.debug("Processing document ID: {}, source: {}", documentId, source);
        }
    }
}
