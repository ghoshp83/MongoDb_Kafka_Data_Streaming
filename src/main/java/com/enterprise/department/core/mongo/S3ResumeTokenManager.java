package com.enterprise.department.core.mongo;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import org.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Implementation of ResumeTokenManager that stores resume tokens in AWS S3.
 */
public class S3ResumeTokenManager implements ResumeTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(S3ResumeTokenManager.class);
    
    private final S3Client s3Client;
    private final String bucketName;
    private final String keyName;
    private final MetricsCollector metricsCollector;
    
    /**
     * Creates a new S3ResumeTokenManager.
     *
     * @param config The application configuration
     * @param metricsCollector The metrics collector
     */
    public S3ResumeTokenManager(Config config, MetricsCollector metricsCollector) {
        this.bucketName = config.getResumeTokenBucket();
        this.keyName = config.getResumeTokenKey();
        this.metricsCollector = metricsCollector;
        
        this.s3Client = S3Client.builder()
                .region(Region.of(config.getAwsRegion()))
                .build();
        
        logger.info("Using S3 resume token storage at s3://{}/{}", bucketName, keyName);
    }
    
    @Override
    public void saveResumeToken(BsonDocument resumeToken) {
        try {
            String json = resumeToken.toJson();
            
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();
            
            s3Client.putObject(request, RequestBody.fromString(json));
            metricsCollector.incrementCounter("resume_token.save");
            logger.debug("Saved resume token to S3: {}", resumeToken);
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            logger.error("S3 error saving resume token: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("resume_token.save.errors");
            throw new RuntimeException("Failed to save resume token to S3", e);
        } catch (Exception e) {
            logger.error("Unexpected error saving resume token to S3: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("resume_token.save.errors");
            throw new RuntimeException("Failed to save resume token", e);
        }
    }
    
    @Override
    public BsonDocument loadResumeToken() {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();
            
            String json = s3Client.getObjectAsBytes(request).asUtf8String();
            BsonDocument resumeToken = BsonDocument.parse(json);
            metricsCollector.incrementCounter("resume_token.load");
            logger.debug("Loaded resume token from S3: {}", resumeToken);
            return resumeToken;
        } catch (NoSuchKeyException e) {
            logger.info("No resume token found in S3");
            metricsCollector.incrementCounter("resume_token.load");
            return null;
        } catch (Exception e) {
            logger.error("Error loading resume token from S3: {}", e.getMessage(), e);
            metricsCollector.incrementCounter("resume_token.load.errors");
            return null;
        }
    }
}
