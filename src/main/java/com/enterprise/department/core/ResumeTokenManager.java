package com.enterprise.department.core;

import org.bson.BsonDocument;

/**
 * Interface for managing resume tokens.
 * Implementations handle storage and retrieval of resume tokens.
 */
public interface ResumeTokenManager {
    /**
     * Load a resume token from storage.
     * 
     * @return The resume token, or null if none exists
     */
    BsonDocument loadResumeToken();
    
    /**
     * Save a resume token to storage.
     * 
     * @param resumeToken The resume token to save
     */
    void saveResumeToken(BsonDocument resumeToken);
    
    /**
     * Check if the initial load has been completed.
     * 
     * @return true if the initial load has been completed, false otherwise
     */
    boolean hasInitialLoadCompleted();
    
    /**
     * Mark the initial load as completed.
     */
    void markInitialLoadCompleted();
    
    /**
     * Clear the initial load completed marker.
     */
    void clearInitialLoadCompleted();
}
