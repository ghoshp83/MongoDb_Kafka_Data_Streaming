package com.enterprise.department.core;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.kafka.KafkaProducerFactory;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.core.mongo.ChangeStreamProcessor;
import com.enterprise.department.core.mongo.InitialLoader;
import com.enterprise.department.core.mongo.MongoFactory;
import com.enterprise.department.core.mongo.ResumeTokenManagerFactory;
import com.enterprise.department.core.mongo.ResumeTokenManager;
import com.enterprise.department.core.shutdown.GracefulShutdownManager;
import com.enterprise.department.health.HealthCheckService;
import com.mongodb.client.MongoClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationContextTest {

    @Mock
    private Config config;
    
    @Test
    void constructor_ShouldInitializeAllComponents() {
        // Arrange
        MongoClient mongoClient = mock(MongoClient.class);
        KafkaProducer<String, String> kafkaProducer = mock(KafkaProducer.class);
        ResumeTokenManager resumeTokenManager = mock(ResumeTokenManager.class);
        
        try (MockedStatic<MongoFactory> mockedMongoFactory = Mockito.mockStatic(MongoFactory.class);
             MockedStatic<KafkaProducerFactory> mockedKafkaFactory = Mockito.mockStatic(KafkaProducerFactory.class);
             MockedStatic<ResumeTokenManagerFactory> mockedResumeTokenFactory = Mockito.mockStatic(ResumeTokenManagerFactory.class)) {
            
            // Mock static factory methods
            mockedMongoFactory.when(() -> MongoFactory.createClient(any(Config.class))).thenReturn(mongoClient);
            mockedKafkaFactory.when(() -> KafkaProducerFactory.createProducer(any(Config.class))).thenReturn(kafkaProducer);
            mockedResumeTokenFactory.when(() -> ResumeTokenManagerFactory.createResumeTokenManager(any(Config.class), any(MetricsCollector.class)))
                .thenReturn(resumeTokenManager);
            
            // Act
            ApplicationContext context = new ApplicationContext(config);
            
            // Assert
            assertNotNull(context.getConfig());
            assertNotNull(context.getInitialLoader());
            assertNotNull(context.getChangeStreamProcessor());
            assertNotNull(context.getHealthCheckService());
            assertNotNull(context.getMetricsCollector());
            assertNotNull(context.getShutdownManager());
        }
    }
}
