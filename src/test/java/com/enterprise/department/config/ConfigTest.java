package com.enterprise.department.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void defaultConstructor_ShouldSetDefaultValues() {
        // When
        AppConfig config = new AppConfig();
        
        // Then
        assertEquals("mongodb://localhost:27017", config.getMongoUri());
        assertEquals("mydb", config.getMongoDatabase());
        assertEquals("mycollection", config.getMongoCollection());
        assertEquals(1000, config.getMongoBatchSize()); // Match the actual value in Config.java
        assertEquals("localhost:9092", config.getKafkaBootstrapServers());
        assertEquals("mongodb-data", config.getKafkaTopic());
        assertEquals("mongo-kafka-cdc", config.getKafkaClientId());
        assertEquals(1048576, config.getKafkaMaxRequestSize());
        assertEquals("all", config.getKafkaAcks());
        assertEquals(true, config.isInitialLoadEnabled());
        assertEquals(false, config.isInitialLoadForce());
        assertEquals(8080, config.getHealthPort());
        assertEquals(5, config.getRetryMaxAttempts());
        assertEquals(1000, config.getRetryBackoffMs());
        assertEquals(100, config.getKafkaBatchSize());
    }
    
    @Test
    void getters_ShouldReturnCorrectValues() {
        // Given
        AppConfig config = new AppConfig();
        
        // When/Then
        assertEquals("mongodb://localhost:27017", config.getMongoUri());
        assertEquals("mydb", config.getMongoDatabase());
        assertEquals("mycollection", config.getMongoCollection());
        assertEquals(1000, config.getMongoBatchSize()); // Match the actual value in Config.java
        assertEquals("mongo-kafka-cdc-tokens", config.getResumeTokenBucket());
        assertEquals("resume-token.json", config.getResumeTokenKey());
        assertEquals("localhost:9092", config.getKafkaBootstrapServers());
        assertEquals("mongodb-data", config.getKafkaTopic());
        assertEquals("mongo-kafka-cdc", config.getKafkaClientId());
        assertEquals(1048576, config.getKafkaMaxRequestSize());
        assertEquals("all", config.getKafkaAcks());
        assertEquals(true, config.isInitialLoadEnabled());
        assertEquals(false, config.isInitialLoadForce());
        assertEquals(8080, config.getHealthPort());
        assertEquals(5, config.getRetryMaxAttempts());
        assertEquals(1000, config.getRetryBackoffMs());
    }
    
    @Test
    void setters_ShouldUpdateValues() {
        // Given
        AppConfig config = new AppConfig();
        
        // When
        config.setMongoUri("mongodb://mongo-host:27017");
        config.setMongoDatabase("customdb");
        config.setMongoCollection("customcollection");
        config.setMongoBatchSize(2000);
        config.setResumeTokenBucket("custom-bucket");
        config.setResumeTokenKey("custom-key.json");
        config.setKafkaBootstrapServers("kafka-host:9092");
        config.setKafkaTopic("custom-topic");
        config.setKafkaClientId("custom-client");
        config.setKafkaMaxRequestSize(2097152);
        config.setKafkaAcks("1");
        config.setInitialLoadEnabled(false);
        config.setInitialLoadForce(true);
        config.setHealthPort(9090);
        config.setRetryMaxAttempts(10);
        config.setRetryBackoffMs(2000);
        config.setKafkaBatchSize(500);
        
        // Then
        assertEquals("mongodb://mongo-host:27017", config.getMongoUri());
        assertEquals("customdb", config.getMongoDatabase());
        assertEquals("customcollection", config.getMongoCollection());
        assertEquals(2000, config.getMongoBatchSize());
        assertEquals("custom-bucket", config.getResumeTokenBucket());
        assertEquals("custom-key.json", config.getResumeTokenKey());
        assertEquals("kafka-host:9092", config.getKafkaBootstrapServers());
        assertEquals("custom-topic", config.getKafkaTopic());
        assertEquals("custom-client", config.getKafkaClientId());
        assertEquals(2097152, config.getKafkaMaxRequestSize());
        assertEquals("1", config.getKafkaAcks());
        assertEquals(false, config.isInitialLoadEnabled());
        assertEquals(true, config.isInitialLoadForce());
        assertEquals(9090, config.getHealthPort());
        assertEquals(10, config.getRetryMaxAttempts());
        assertEquals(2000, config.getRetryBackoffMs());
        assertEquals(500, config.getKafkaBatchSize());
    }
}
