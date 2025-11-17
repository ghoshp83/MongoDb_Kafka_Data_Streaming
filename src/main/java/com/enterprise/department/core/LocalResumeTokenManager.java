package com.enterprise.department.core;

import com.enterprise.department.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of ResumeTokenManager that stores resume tokens in local file system.
 * This is used when running locally without AWS credentials.
 */
@Slf4j
public class LocalResumeTokenManager implements ResumeTokenManager {
    private static final String LOCAL_STORAGE_DIR = ".mongo-kafka-cdc";
    private static final String RESUME_TOKEN_FILENAME = "resume-token.json";
    private static final String INITIAL_LOAD_FILENAME = "initial-load-completed.flag";
    
    private final Path resumeTokenPath;
    private final Path initialLoadPath;
    
    /**
     * Create a new LocalResumeTokenManager.
     * 
     * @param config The application configuration
     */
    public LocalResumeTokenManager(Config config) {
        // Create local storage directory if it doesn't exist
        Path storageDir = Paths.get(System.getProperty("user.home"), LOCAL_STORAGE_DIR);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            log.warn("Failed to create local storage directory: {}", storageDir, e);
        }
        
        // Set paths for resume token and initial load flag
        String tokenKey = config.getResumeTokenKey();
        if (tokenKey == null || tokenKey.isEmpty()) {
            tokenKey = RESUME_TOKEN_FILENAME;
        }
        
        resumeTokenPath = storageDir.resolve(tokenKey);
        initialLoadPath = storageDir.resolve(INITIAL_LOAD_FILENAME);
        
        log.warn("Using local resume token storage at: {}", resumeTokenPath);
    }

    @Override
    public BsonDocument loadResumeToken() {
        try {
            if (!Files.exists(resumeTokenPath)) {
                log.warn("No resume token found at: {}", resumeTokenPath);
                return null;
            }
            
            String tokenJson = new String(Files.readAllBytes(resumeTokenPath), StandardCharsets.UTF_8);
            log.warn("Loaded resume token from local file: {}", resumeTokenPath);
            
            return BsonDocument.parse(tokenJson);
        } catch (Exception e) {
            log.warn("Failed to load resume token from local file: {}", resumeTokenPath, e);
            return null;
        }
    }

    @Override
    public void saveResumeToken(BsonDocument resumeToken) {
        if (resumeToken == null) {
            log.warn("Attempted to save null resume token, ignoring");
            return;
        }
        
        try {
            String tokenJson = resumeToken.toJson();
            Files.write(resumeTokenPath, tokenJson.getBytes(StandardCharsets.UTF_8));
            log.debug("Saved resume token to local file: {}", resumeTokenPath);
        } catch (Exception e) {
            log.warn("Failed to save resume token to local file: {}", resumeTokenPath, e);
        }
    }

    @Override
    public boolean hasInitialLoadCompleted() {
        return Files.exists(initialLoadPath);
    }

    @Override
    public void markInitialLoadCompleted() {
        try {
            Files.write(initialLoadPath, "completed".getBytes(StandardCharsets.UTF_8));
            log.warn("Marked initial load as completed in local file: {}", initialLoadPath);
        } catch (Exception e) {
            log.warn("Failed to mark initial load as completed in local file: {}", initialLoadPath, e);
        }
    }
    
    @Override
    public void clearInitialLoadCompleted() {
        try {
            if (Files.exists(initialLoadPath)) {
                Files.delete(initialLoadPath);
                log.warn("Cleared initial load completed marker: {}", initialLoadPath);
            }
        } catch (Exception e) {
            log.warn("Failed to clear initial load completed marker: {}", initialLoadPath, e);
        }
    }
}
