package com.enterprise.department.core.mongo;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating ResumeTokenManager instances.
 */
public class ResumeTokenManagerFactory {
    private static final Logger logger = LoggerFactory.getLogger(ResumeTokenManagerFactory.class);
    
    /**
     * Creates a ResumeTokenManager based on the environment.
     * If AWS credentials are available, returns an S3ResumeTokenManager.
     * Otherwise, returns a LocalResumeTokenManager.
     *
     * @param config The application configuration
     * @param metricsCollector The metrics collector
     * @return A ResumeTokenManager instance
     */
    public static ResumeTokenManager createResumeTokenManager(Config config, MetricsCollector metricsCollector) {
        // Check if AWS credentials are available
        String awsAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String awsSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        if (awsAccessKey != null && !awsAccessKey.isEmpty() && 
            awsSecretKey != null && !awsSecretKey.isEmpty()) {
            logger.info("AWS credentials found, using S3 for resume token storage");
            return new S3ResumeTokenManager(config, metricsCollector);
        } else {
            logger.info("No AWS credentials found, using local file system for resume token storage");
            return new LocalResumeTokenManager(config, metricsCollector);
        }
    }
}
