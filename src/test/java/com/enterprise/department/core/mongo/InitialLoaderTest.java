package com.enterprise.department.core.mongo;

import com.enterprise.department.core.process.DocumentProcessingStrategy;
import com.enterprise.department.config.Config;
import com.enterprise.department.core.metrics.MetricsCollector;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InitialLoaderTest {

    @Mock
    private MongoClient mongoClient;
    
    @Mock
    private KafkaProducer<String, String> kafkaProducer;
    
    @Mock
    private Config config;
    
    @Mock
    private MetricsCollector metricsCollector;
    
    @Mock
    private MongoDatabase database;
    
    @Mock
    private MongoCollection<Document> collection;
    
    @Mock
    private FindIterable<Document> findIterable;
    
    @Mock
    private MongoCursor<Document> cursor;
    
    @Mock
    private DocumentProcessingStrategy processingStrategy;
    
    @Mock
    private MetricsCollector.Timer timer;
    
    private InitialLoader initialLoader;
    
    @BeforeEach
    void setUp() {
        when(config.getMongoDatabase()).thenReturn("testdb");
        when(config.getMongoCollection()).thenReturn("testcollection");
        when(mongoClient.getDatabase("testdb")).thenReturn(database);
        when(database.getCollection("testcollection")).thenReturn(collection);
        
        initialLoader = new InitialLoader(mongoClient, config, metricsCollector, processingStrategy);
    }
    
    @Test
    void loadInitialData_ShouldProcessAllDocuments() throws Exception {
        // Given
        List<Document> documents = new ArrayList<>();
        documents.add(new Document("_id", "1").append("name", "Document 1"));
        documents.add(new Document("_id", "2").append("name", "Document 2"));
        documents.add(new Document("_id", "3").append("name", "Document 3"));
        
        when(config.isInitialLoadEnabled()).thenReturn(true);
        when(config.isInitialLoadForce()).thenReturn(true);
        when(config.getMongoBatchSize()).thenReturn(10);
        when(config.getKafkaTopic()).thenReturn("test-topic");
        when(collection.find()).thenReturn(findIterable);
        when(findIterable.batchSize(anyInt())).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, true, false);
        when(cursor.next()).thenReturn(documents.get(0), documents.get(1), documents.get(2));
        
        when(metricsCollector.startTimer(anyString())).thenReturn(timer);
        
        // When
        initialLoader.loadInitialData();
        
        // Then
        verify(processingStrategy, times(3)).processDocument(any(Document.class), eq("read"), eq("initial_load"));
        verify(metricsCollector).startTimer("initial_load.duration");
    }
    
    @Test
    void loadInitialData_WhenNoDocuments_ShouldNotProcessAny() throws Exception {
        // Given
        when(config.isInitialLoadEnabled()).thenReturn(true);
        when(config.isInitialLoadForce()).thenReturn(true);
        when(config.getMongoBatchSize()).thenReturn(10);
        when(collection.find()).thenReturn(findIterable);
        when(findIterable.batchSize(anyInt())).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        when(metricsCollector.startTimer(anyString())).thenReturn(timer);
        
        // When
        initialLoader.loadInitialData();
        
        // Then
        verify(processingStrategy, never()).processDocument(any(Document.class), anyString(), anyString());
    }
    
    @Test
    void loadInitialData_WhenInitialLoadDisabled_ShouldNotProcess() throws Exception {
        // Given
        when(config.isInitialLoadEnabled()).thenReturn(false);
        
        // When
        initialLoader.loadInitialData();
        
        // Then
        verify(collection, never()).find();
        verify(kafkaProducer, never()).send(any(ProducerRecord.class));
    }
}
