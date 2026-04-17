package com.github.ghoshp83.mongokafkastream.core;

import com.github.ghoshp83.mongokafkastream.config.Config;
import com.github.ghoshp83.mongokafkastream.core.kafka.KafkaProducerFactory;
import com.github.ghoshp83.mongokafkastream.core.metrics.MetricsCollector;
import com.github.ghoshp83.mongokafkastream.core.mongo.ChangeStreamProcessor;
import com.github.ghoshp83.mongokafkastream.core.mongo.InitialLoader;
import com.github.ghoshp83.mongokafkastream.core.mongo.MongoFactory;
import com.github.ghoshp83.mongokafkastream.core.mongo.ResumeTokenManagerFactory;
import com.github.ghoshp83.mongokafkastream.core.mongo.ResumeTokenManager;
import com.github.ghoshp83.mongokafkastream.core.shutdown.GracefulShutdownManager;
import com.github.ghoshp83.mongokafkastream.health.HealthCheckService;
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
