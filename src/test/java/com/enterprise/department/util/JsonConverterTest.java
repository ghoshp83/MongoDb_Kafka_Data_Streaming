package com.enterprise.department.util;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonConverterTest {

    @Test
    void documentToJson_WithSimpleDocument_ShouldConvertCorrectly() {
        // Given
        Document document = new Document("_id", "test123")
                .append("name", "Test Document")
                .append("value", 42);
        
        // When
        String json = JsonConverter.documentToJson(document);
        
        // Then
        assertTrue(json.contains("\"_id\":\"test123\""));
        assertTrue(json.contains("\"name\":\"Test Document\""));
        assertTrue(json.contains("\"value\":42"));
    }
    
    @Test
    void documentToJson_WithNestedDocument_ShouldConvertCorrectly() {
        // Given
        Document nestedDoc = new Document("subfield", "subvalue");
        Document document = new Document("_id", "test123")
                .append("name", "Test Document")
                .append("nested", nestedDoc);
        
        // When
        String json = JsonConverter.documentToJson(document);
        
        // Then
        assertTrue(json.contains("\"_id\":\"test123\""));
        assertTrue(json.contains("\"name\":\"Test Document\""));
        assertTrue(json.contains("\"nested\":{\"subfield\":\"subvalue\"}"));
    }
    
    @Test
    void documentToJson_WithNullDocument_ShouldReturnEmptyJson() {
        // When
        String json = JsonConverter.documentToJson(null);
        
        // Then
        assertEquals("{}", json);
    }
    
    @Test
    void parseJson_WithValidJson_ShouldReturnJsonNode() {
        // Given
        String json = "{\"name\":\"Test\",\"value\":42}";
        
        // When
        com.fasterxml.jackson.databind.JsonNode node = JsonConverter.parseJson(json);
        
        // Then
        assertEquals("Test", node.get("name").asText());
        assertEquals(42, node.get("value").asInt());
    }
    
    @Test
    void parseJson_WithInvalidJson_ShouldReturnEmptyNode() {
        // Given
        String json = "invalid json";
        
        // When
        com.fasterxml.jackson.databind.JsonNode node = JsonConverter.parseJson(json);
        
        // Then
        assertTrue(node.isObject());
        assertEquals(0, node.size());
    }
}
