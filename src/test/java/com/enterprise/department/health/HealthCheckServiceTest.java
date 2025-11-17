package com.enterprise.department.health;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private Config config;
    
    @Mock
    private MongoClient mongoClient;
    
    @Mock
    private KafkaProducer<String, String> kafkaProducer;
    
    @Mock
    private MetricsCollector metricsCollector;
    
    @Mock
    private MongoDatabase database;
    
    private HealthCheckService healthCheckService;
    
    @BeforeEach
    void setUp() {
        when(config.getMongoDatabase()).thenReturn("testdb");
        when(config.getKafkaTopic()).thenReturn("test-topic");
        when(mongoClient.getDatabase(anyString())).thenReturn(database);
        when(metricsCollector.getMetricsReport()).thenReturn("Test metrics report");
        
        healthCheckService = new HealthCheckService(config, mongoClient, kafkaProducer, metricsCollector);
    }
    
    @Test
    void checkHealth_WhenAllServicesHealthy_ShouldReturnHealthy() {
        // Given
        // No need to stub void methods with doNothing
        
        // When
        HealthStatus status = healthCheckService.checkHealth();
        
        // Then
        assertTrue(status.isHealthy());
        assertEquals(3, status.getComponents().size());
        assertTrue(status.getComponents().get("mongodb").isHealthy());
        assertTrue(status.getComponents().get("kafka").isHealthy());
        assertTrue(status.getComponents().get("application").isHealthy());
        assertEquals("Test metrics report", status.getMetrics());
    }
    
    @Test
    void checkHealth_WhenMongoDbUnhealthy_ShouldReturnUnhealthy() {
        // Given
        doThrow(new RuntimeException("MongoDB connection failed")).when(database).runCommand(any(Document.class));
        
        // When
        HealthStatus status = healthCheckService.checkHealth();
        
        // Then
        assertFalse(status.isHealthy());
        assertEquals(3, status.getComponents().size());
        assertFalse(status.getComponents().get("mongodb").isHealthy());
        assertTrue(status.getComponents().get("kafka").isHealthy());
        assertFalse(status.getComponents().get("application").isHealthy());
    }
    
    @Test
    void checkHealth_WhenKafkaUnhealthy_ShouldReturnUnhealthy() {
        // Given
        doThrow(new RuntimeException("Kafka connection failed")).when(kafkaProducer).partitionsFor(anyString());
        
        // When
        HealthStatus status = healthCheckService.checkHealth();
        
        // Then
        assertFalse(status.isHealthy());
        assertEquals(3, status.getComponents().size());
        assertTrue(status.getComponents().get("mongodb").isHealthy());
        assertFalse(status.getComponents().get("kafka").isHealthy());
        assertFalse(status.getComponents().get("application").isHealthy());
    }
    
    @Test
    void isReady_WhenAllServicesHealthy_ShouldReturnTrue() {
        // Given
        // No need to stub void methods with doNothing
        
        // When
        boolean ready = healthCheckService.isReady();
        
        // Then
        assertTrue(ready);
    }
    
    @Test
    void isReady_WhenAnyServiceUnhealthy_ShouldReturnFalse() {
        // Given
        doThrow(new RuntimeException("MongoDB connection failed")).when(database).runCommand(any(Document.class));
        
        // When
        boolean ready = healthCheckService.isReady();
        
        // Then
        assertFalse(ready);
    }
    
    @Test
    void getComponentHealths_ShouldReturnAllComponents() {
        // Given
        // No need to stub void methods with doNothing
        
        // When
        List<ComponentHealth> components = healthCheckService.getComponentHealths();
        
        // Then
        assertEquals(3, components.size());
    }
}
