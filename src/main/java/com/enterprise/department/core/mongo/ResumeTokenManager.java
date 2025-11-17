package com.enterprise.department.core.mongo;

import org.bson.BsonDocument;

/**
 * Interface for managing resume tokens.
 */
public interface ResumeTokenManager {
    
    /**
     * Saves a resume token.
     *
     * @param resumeToken The resume token to save
     */
    void saveResumeToken(BsonDocument resumeToken);
    
    /**
     * Loads a resume token.
     *
     * @return The loaded resume token, or null if no token is available
     */
    BsonDocument loadResumeToken();
}
