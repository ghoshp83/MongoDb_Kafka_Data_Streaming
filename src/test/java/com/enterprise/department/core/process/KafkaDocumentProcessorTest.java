package com.enterprise.department.core.process;

import com.enterprise.department.core.kafka.BatchKafkaProducer;
import com.enterprise.department.core.kafka.KafkaDocumentProcessor;
import com.enterprise.department.core.metrics.MetricsCollector;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaDocumentProcessorTest {

    @Mock
    private BatchKafkaProducer kafkaProducer;
    
    @Mock
    private MetricsCollector metricsCollector;
    
    private KafkaDocumentProcessor processor;
    
    @BeforeEach
    void setUp() {
        processor = new KafkaDocumentProcessor(kafkaProducer, metricsCollector);
    }

    @Test
    void processDocument_ShouldSendToKafka() {
        // Arrange
        Document document = new Document("_id", "test123")
                .append("name", "Test Document")
                .append("value", 42);
        
        // Act
        processor.processDocument(document, "read", "initial_load");
        
        // Assert
        verify(kafkaProducer).send(eq("test123"), anyString());
        verify(metricsCollector).incrementCounter("kafka.documents.processed");
        verify(metricsCollector).incrementCounter("kafka.documents.read");
    }

    @Test
    void processDocument_WithVuid_ShouldUseVuidAsKey() {
        // Arrange
        Document document = new Document("_id", "test123")
                .append("vuid", "vehicle123")
                .append("name", "Test Document")
                .append("value", 42);
        
        // Act
        processor.processDocument(document, "insert", "change_stream");
        
        // Assert
        verify(kafkaProducer).send(eq("vehicle123"), anyString());
        verify(metricsCollector).incrementCounter("kafka.documents.processed");
    }

    @Test
    void processDocument_WithoutVuid_ShouldUseIdAsKey() {
        // Arrange
        Document document = new Document("_id", "test123")
                .append("name", "Test Document")
                .append("value", 42);
        
        // Act
        processor.processDocument(document, "update", "change_stream");
        
        // Assert
        verify(kafkaProducer).send(eq("test123"), anyString());
        verify(metricsCollector).incrementCounter("kafka.documents.processed");
    }

    @Test
    void processDocument_WithError_ShouldHandleGracefully() {
        // Arrange
        Document document = new Document("_id", "test123");
        doThrow(new RuntimeException("Test exception")).when(kafkaProducer).send(anyString(), anyString());
        
        // Act
        processor.processDocument(document, "delete", "change_stream");
        
        // Assert
        verify(metricsCollector).incrementCounter("kafka.documents.errors");
    }
    
    @Test
    void close_ShouldFlushKafkaProducer() {
        // Act
        processor.close();
        
        // Assert
        verify(kafkaProducer).flush();
    }
}
