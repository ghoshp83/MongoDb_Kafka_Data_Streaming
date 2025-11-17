package com.enterprise.department.util;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for converting between different document formats.
 */
public class DocumentConverter {

    private static final JsonWriterSettings JSON_WRITER_SETTINGS = 
            JsonWriterSettings.builder()
                    .outputMode(JsonMode.RELAXED)
                    .build();
    
    private static final DateTimeFormatter ISO_FORMATTER = 
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));

    /**
     * Converts a BsonDocument to a Document.
     *
     * @param bsonDocument The BsonDocument to convert
     * @return The converted Document
     */
    public static Document toDocument(BsonDocument bsonDocument) {
        if (bsonDocument == null) {
            return null;
        }
        return Document.parse(bsonDocument.toJson(JSON_WRITER_SETTINGS));
    }

    /**
     * Converts a Document to a BsonDocument.
     *
     * @param document The Document to convert
     * @return The converted BsonDocument
     */
    public static BsonDocument toBsonDocument(Document document) {
        if (document == null) {
            return null;
        }
        return BsonDocument.parse(document.toJson(JSON_WRITER_SETTINGS));
    }

    /**
     * Extracts the ID from a document.
     *
     * @param document The document
     * @return The ID as a string
     */
    public static String extractId(Document document) {
        if (document == null) {
            return null;
        }
        
        // First check for a vuid field
        if (document.containsKey("vuid")) {
            Object vuid = document.get("vuid");
            return vuid != null ? vuid.toString() : null;
        }
        
        // Fall back to _id field
        if (document.containsKey("_id")) {
            Object id = document.get("_id");
            return id != null ? id.toString() : null;
        }
        
        return null;
    }

    /**
     * Converts a Document to a JSON string.
     *
     * @param document The document to convert
     * @return The JSON string
     */
    public static String convertToJsonObject(Document document) {
        if (document == null) {
            return "{}";
        }
        return document.toJson(JSON_WRITER_SETTINGS);
    }
    
    /**
     * Converts a Document to a JSON string.
     *
     * @param document The document to convert
     * @return The JSON string
     */
    public static String convertToJsonString(Document document) {
        if (document == null) {
            return "{}";
        }
        return document.toJson(JSON_WRITER_SETTINGS);
    }
    
    /**
     * Adds metadata to a document.
     *
     * @param document The document to add metadata to
     * @param operation The operation type
     * @param source The source of the document
     * @return The document with metadata
     */
    public static Document addMetadata(Document document, String operation, String source) {
        if (document == null) {
            return null;
        }
        
        document.append("_operation", operation);
        document.append("_source", source);
        document.append("_timestamp", ISO_FORMATTER.format(Instant.now()));
        
        return document;
    }
}
