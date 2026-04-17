package com.github.ghoshp83.mongokafkastream.core.process;

import com.github.ghoshp83.mongokafkastream.api.DocumentProcessingStrategy;
import com.github.ghoshp83.mongokafkastream.core.DocumentLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.List;

/**
 * Processes batches of MongoDB documents.
 */
@Slf4j
@RequiredArgsConstructor
public class BatchDocumentProcessor {
    
    private final DocumentProcessingStrategy processingStrategy;
    
    /**
     * Process a batch of documents.
     *
     * @param documents The documents to process
     * @param operation The operation type (read, insert, update, replace, delete)
     * @param source The source of the event (initial_load or change_stream)
     */
    public void processDocuments(List<Document> documents, String operation, String source) {
        if (documents == null || documents.isEmpty()) {
            log.debug("No documents to process");
            return;
        }
        
        log.debug("Processing batch of {} documents", documents.size());
        
        // Process each document
        documents.forEach(document -> {
            try {
                DocumentLogger.logDocument(document, operation, source);
                processingStrategy.processDocument(document, operation, source);
            } catch (Exception e) {
                log.error("Error processing document", e);
            }
        });
    }
}
