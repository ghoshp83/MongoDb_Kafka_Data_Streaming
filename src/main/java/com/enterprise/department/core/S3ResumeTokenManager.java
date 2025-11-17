package com.enterprise.department.core;

import com.enterprise.department.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.json.JSONObject;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;

/**
 * Implementation of ResumeTokenManager that stores resume tokens in AWS S3.
 */
@Slf4j
public class S3ResumeTokenManager implements ResumeTokenManager {
    private static final String INITIAL_LOAD_COMPLETED_KEY_SUFFIX = "-initial-load-completed";
    
    private final S3Client s3Client;
    private final String bucketName;
    private final String resumeTokenKey;
    private final String initialLoadCompletedKey;
    
    /**
     * Create a new S3ResumeTokenManager.
     * 
     * @param config The application configuration
     */
    public S3ResumeTokenManager(Config config) {
        // Get AWS region from environment or config
        String awsRegion = System.getenv("AWS_REGION");
        if (awsRegion == null || awsRegion.isEmpty()) {
            awsRegion = "us-east-1"; // Default region
            log.warn("AWS_REGION not set, using default: {}", awsRegion);
        }
        
        // Create S3 client
        this.s3Client = S3Client.builder()
                .region(Region.of(awsRegion))
                .build();
        
        // Get bucket and key from config
        this.bucketName = config.getResumeTokenBucket();
        this.resumeTokenKey = config.getResumeTokenKey();
        this.initialLoadCompletedKey = resumeTokenKey + INITIAL_LOAD_COMPLETED_KEY_SUFFIX;
        
        log.warn("Using S3 resume token storage: s3://{}/{}", bucketName, resumeTokenKey);
    }

    @Override
    public BsonDocument loadResumeToken() {
        try {
            // Get the object and read its content
            String tokenJson;
            try (var objectResponse = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(resumeTokenKey)
                    .build())) {
                tokenJson = new String(objectResponse.readAllBytes(), StandardCharsets.UTF_8);
                log.warn("Loaded resume token from S3: {}", resumeTokenKey);
            }
            
            // Parse the JSON string to a BsonDocument
            return BsonDocument.parse(tokenJson);
        } catch (NoSuchKeyException e) {
            log.warn("No resume token found in S3: {}", resumeTokenKey);
            return null;
        } catch (Exception e) {
            log.warn("Failed to load resume token from S3: {}", resumeTokenKey, e);
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
            // Convert BsonDocument to JSON string
            String tokenJson = resumeToken.toJson();
            
            // Upload the JSON string to S3
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(resumeTokenKey)
                    .build(),
                    RequestBody.fromString(tokenJson));
            
            log.debug("Saved resume token to S3: {}", resumeTokenKey);
        } catch (Exception e) {
            log.warn("Failed to save resume token to S3: {}", resumeTokenKey, e);
        }
    }

    @Override
    public boolean hasInitialLoadCompleted() {
        try {
            s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(initialLoadCompletedKey)
                    .build());
            
            log.warn("Initial load has been completed previously");
            return true;
        } catch (NoSuchKeyException e) {
            log.warn("Initial load has not been completed yet");
            return false;
        } catch (Exception e) {
            log.warn("Failed to check if initial load has been completed", e);
            return false;
        }
    }

    @Override
    public void markInitialLoadCompleted() {
        try {
            JSONObject marker = new JSONObject();
            marker.put("completed", true);
            marker.put("timestamp", System.currentTimeMillis());
            
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(initialLoadCompletedKey)
                    .build(),
                    RequestBody.fromString(marker.toString()));
            
            log.warn("Marked initial load as completed in S3: {}", initialLoadCompletedKey);
        } catch (Exception e) {
            log.warn("Failed to mark initial load as completed in S3: {}", initialLoadCompletedKey, e);
        }
    }
    
    @Override
    public void clearInitialLoadCompleted() {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(initialLoadCompletedKey)
                    .build());
            
            log.warn("Cleared initial load completed marker in S3: {}", initialLoadCompletedKey);
        } catch (NoSuchKeyException e) {
            // Already deleted, nothing to do
            log.warn("Initial load completed marker not found in S3, nothing to clear");
        } catch (Exception e) {
            log.warn("Failed to clear initial load completed marker in S3: {}", initialLoadCompletedKey, e);
        }
    }
}
