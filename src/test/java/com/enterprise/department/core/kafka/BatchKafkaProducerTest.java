package com.enterprise.department.core.kafka;

import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchKafkaProducerTest {

    @Mock
    private KafkaProducer<String, String> kafkaProducer;
    
    @Mock
    private Config config;
    
    @Mock
    private MetricsCollector metricsCollector;
    
    @Captor
    private ArgumentCaptor<ProducerRecord<String, String>> recordCaptor;
    
    private BatchKafkaProducer batchProducer;
    
    @BeforeEach
    void setUp() {
        when(config.getKafkaTopic()).thenReturn("test-topic");
        when(config.getKafkaBatchSize()).thenReturn(10);
        
        batchProducer = new BatchKafkaProducer(kafkaProducer, config, metricsCollector);
    }

    @Test
    void send_ShouldAddMessageToBatch() {
        // Arrange
        String key = "test-key";
        String value = "test-value";
        
        // Act
        batchProducer.send(key, value);
        
        // Assert - should not flush until batch size is reached
        verify(kafkaProducer, never()).send(any(), any());
        verify(kafkaProducer, never()).flush();
        verify(metricsCollector, never()).incrementCounter("kafka.batch.flush");
    }

    @Test
    void flush_WhenBatchIsEmpty_ShouldDoNothing() {
        // Act
        batchProducer.flush();
        
        // Assert
        verify(kafkaProducer, never()).send(any(), any());
        verify(kafkaProducer, never()).flush();
    }

    @Test
    void flush_WhenBatchHasMessages_ShouldSendAndClearBatch() {
        // Arrange
        batchProducer.send("key1", "value1");
        
        // Act
        batchProducer.flush();
        
        // Assert
        verify(kafkaProducer).send(recordCaptor.capture(), any());
        verify(kafkaProducer).flush();
        verify(metricsCollector).incrementCounter("kafka.batch.flush");
        
        ProducerRecord<String, String> capturedRecord = recordCaptor.getValue();
        assertEquals("test-topic", capturedRecord.topic());
        assertEquals("key1", capturedRecord.key());
        assertEquals("value1", capturedRecord.value());
    }

    @Test
    void flush_WhenExceptionOccurs_ShouldHandleGracefully() {
        // Arrange
        batchProducer.send("key1", "value1");
        doThrow(new RuntimeException("Test exception")).when(kafkaProducer).flush();
        
        // Act
        batchProducer.flush();
        
        // Assert
        verify(metricsCollector).incrementCounter("kafka.batch.errors");
    }

    @Test
    void close_ShouldFlushBatch() {
        // Arrange
        batchProducer.send("key1", "value1");
        
        // Act
        batchProducer.close();
        
        // Assert
        verify(kafkaProducer).send(any(), any());
        verify(kafkaProducer).flush();
    }
    
    @Test
    void send_WhenBatchSizeReached_ShouldFlush() {
        // Arrange - batch size is 10
        for (int i = 0; i < 9; i++) {
            batchProducer.send("key" + i, "value" + i);
        }
        
        // Act - this should trigger flush
        batchProducer.send("key9", "value9");
        
        // Assert
        verify(kafkaProducer, times(10)).send(any(), any());
        verify(kafkaProducer).flush();
        verify(metricsCollector).incrementCounter("kafka.batch.flush");
    }
}
