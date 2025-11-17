package com.enterprise.department.core;

import com.enterprise.department.config.Config;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating resume token managers.
 * Uses the Factory pattern to create the appropriate implementation.
 */
@Slf4j
public class ResumeTokenManagerFactory {
    
    /**
     * Create a resume token manager based on the environment.
     * If AWS credentials are available, an S3ResumeTokenManager is created.
     * Otherwise, a LocalResumeTokenManager is created.
     * 
     * @param config The application configuration
     * @return A resume token manager
     */
    public static ResumeTokenManager createResumeTokenManager(Config config) {
        // Check if AWS credentials are available
        String awsAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String awsSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        if (awsAccessKey != null && !awsAccessKey.isEmpty() && 
            awsSecretKey != null && !awsSecretKey.isEmpty()) {
            log.warn("AWS credentials found, using S3ResumeTokenManager");
            return new S3ResumeTokenManager(config);
        } else {
            log.warn("AWS credentials not found, using LocalResumeTokenManager");
            return new LocalResumeTokenManager(config);
        }
    }
}
