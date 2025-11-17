package com.enterprise.department.core.mongo;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import org.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of ResumeTokenManager that stores resume tokens in the local file system.
 */
public class LocalResumeTokenManager implements ResumeTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(LocalResumeTokenManager.class);
    
    private final Path resumeTokenPath;
    private final MetricsCollector metricsCollector;
    
    /**
     * Creates a new LocalResumeTokenManager.
     *
     * @param config The application configuration
     * @param metricsCollector The metrics collector
     */
    public LocalResumeTokenManager(Config config, MetricsCollector metricsCollector) {
        this.resumeTokenPath = Paths.get(config.getResumeTokenPath());
        this.metricsCollector = metricsCollector;
        logger.info("Using local resume token storage at: {}", resumeTokenPath);
    }
    
    @Override
    public void saveResumeToken(BsonDocument resumeToken) {
        try {
            // Check if parent directory exists and create it if needed
            Path parent = resumeTokenPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            
            // Write the resume token to file
            Files.writeString(resumeTokenPath, resumeToken.toJson());
            metricsCollector.incrementCounter("resume_token.save");
            logger.debug("Saved resume token: {}", resumeToken);
        } catch (IOException e) {
            logger.error("Error saving resume token: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("resume_token.save.errors");
        }
    }
    
    @Override
    public BsonDocument loadResumeToken() {
        try {
            if (Files.exists(resumeTokenPath)) {
                String json = Files.readString(resumeTokenPath);
                BsonDocument resumeToken = BsonDocument.parse(json);
                metricsCollector.incrementCounter("resume_token.load");
                logger.debug("Loaded resume token: {}", resumeToken);
                return resumeToken;
            }
        } catch (IOException e) {
            logger.error("Error loading resume token: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("resume_token.load.errors");
        }
        
        metricsCollector.incrementCounter("resume_token.load");
        return null;
    }
}
