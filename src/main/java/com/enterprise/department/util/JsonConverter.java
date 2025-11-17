package com.enterprise.department.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

/**
 * Utility class for converting between MongoDB documents and JSON.
 */
@Slf4j
public class JsonConverter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private JsonConverter() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Convert a MongoDB document to a JSON string.
     *
     * @param document The MongoDB document
     * @return JSON string representation
     */
    public static String documentToJson(Document document) {
        if (document == null) {
            return "{}";
        }
        
        try {
            // Convert MongoDB document to a Map
            Map<String, Object> documentMap = document;
            
            // Handle special MongoDB types
            handleSpecialTypes(documentMap);
            
            // Convert to JSON
            return objectMapper.writeValueAsString(documentMap);
        } catch (JsonProcessingException e) {
            log.error("Error converting document to JSON", e);
            return "{}";
        }
    }
    
    /**
     * Handle special MongoDB types that need custom serialization.
     *
     * @param documentMap The document as a map
     */
    private static void handleSpecialTypes(Map<String, Object> documentMap) {
        for (Map.Entry<String, Object> entry : documentMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof ObjectId) {
                // Convert ObjectId to string
                documentMap.put(key, value.toString());
            } else if (value instanceof Date) {
                // Convert Date to ISO string
                documentMap.put(key, ((Date) value).toInstant().toString());
            } else if (value instanceof Map) {
                // Recursively handle nested maps
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                handleSpecialTypes(nestedMap);
            }
        }
    }
    
    /**
     * Add metadata fields to a JSON object.
     *
     * @param jsonObject The JSON object
     * @param operation The operation type
     * @param source The source of the event
     * @return The JSON object with metadata
     */
    public static ObjectNode addMetadata(ObjectNode jsonObject, String operation, String source) {
        jsonObject.put("_operation", operation);
        jsonObject.put("_source", source);
        jsonObject.put("_timestamp", new Date().toInstant().toString());
        return jsonObject;
    }
    
    /**
     * Parse a JSON string into a JsonNode.
     *
     * @param json The JSON string
     * @return The parsed JsonNode
     */
    public static JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON", e);
            return objectMapper.createObjectNode();
        }
    }
}
