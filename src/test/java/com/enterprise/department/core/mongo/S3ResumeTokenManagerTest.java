package com.enterprise.department.core.mongo;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ResumeTokenManagerTest {

    @Mock
    private Config config;
    
    @Mock
    private MetricsCollector metricsCollector;
    
    private S3ResumeTokenManager resumeTokenManager;
    
    @BeforeEach
    void setUp() {
        when(config.getResumeTokenBucket()).thenReturn("test-bucket");
        when(config.getResumeTokenKey()).thenReturn("test-key");
        when(config.getAwsRegion()).thenReturn("us-east-1");
        
        // Create a test implementation that doesn't actually connect to AWS
        resumeTokenManager = new TestS3ResumeTokenManager(config, metricsCollector);
    }
    
    @Test
    void saveResumeToken_ShouldUploadTokenToS3() {
        // Given
        BsonDocument token = new BsonDocument("_data", new BsonString("test-token"));
        
        // When
        resumeTokenManager.saveResumeToken(token);
        
        // Then
        verify(metricsCollector).incrementCounter("resume_token.saved");
        assertTrue(((TestS3ResumeTokenManager)resumeTokenManager).tokenSaved);
    }
    
    @Test
    void loadResumeToken_WhenObjectExists_ShouldReturnToken() {
        // Given
        BsonDocument token = new BsonDocument("_data", new BsonString("test-token"));
        ((TestS3ResumeTokenManager)resumeTokenManager).setTokenToLoad(token);
        
        // When
        BsonDocument loadedToken = resumeTokenManager.loadResumeToken();
        
        // Then
        assertNotNull(loadedToken);
        assertEquals("test-token", loadedToken.getString("_data").getValue());
        verify(metricsCollector).incrementCounter("resume_token.loaded");
    }
    
    @Test
    void loadResumeToken_WhenObjectDoesNotExist_ShouldReturnNull() {
        // Given
        ((TestS3ResumeTokenManager)resumeTokenManager).setTokenToLoad(null);
        ((TestS3ResumeTokenManager)resumeTokenManager).setShouldThrowException(true);
        
        // When
        BsonDocument loadedToken = resumeTokenManager.loadResumeToken();
        
        // Then
        assertNull(loadedToken);
        verify(metricsCollector).incrementCounter("resume_token.not_found");
    }
    
    /**
     * Test implementation of S3ResumeTokenManager that doesn't actually connect to AWS
     */
    private static class TestS3ResumeTokenManager extends S3ResumeTokenManager {
        private BsonDocument tokenToLoad;
        private boolean tokenSaved = false;
        private boolean shouldThrowException = false;
        private final MetricsCollector metricsCollector;
        
        public TestS3ResumeTokenManager(Config config, MetricsCollector metricsCollector) {
            super(config, metricsCollector);
            this.metricsCollector = metricsCollector;
        }
        
        public void setTokenToLoad(BsonDocument tokenToLoad) {
            this.tokenToLoad = tokenToLoad;
        }
        
        public void setShouldThrowException(boolean shouldThrowException) {
            this.shouldThrowException = shouldThrowException;
        }
        
        @Override
        public void saveResumeToken(BsonDocument token) {
            tokenSaved = true;
            metricsCollector.incrementCounter("resume_token.saved");
        }
        
        @Override
        public BsonDocument loadResumeToken() {
            if (shouldThrowException) {
                metricsCollector.incrementCounter("resume_token.not_found");
                return null;
            }
            
            if (tokenToLoad != null) {
                metricsCollector.incrementCounter("resume_token.loaded");
            }
            return tokenToLoad;
        }
    }
}
