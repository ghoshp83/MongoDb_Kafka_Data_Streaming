package com.enterprise.department.core;

import com.enterprise.department.config.Config;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Factory for creating MongoDB clients.
 */
public class MongoFactory {
    private static final Logger logger = LoggerFactory.getLogger(MongoFactory.class);
    
    /**
     * Create a new MongoDB client.
     * 
     * @param config The application configuration
     * @return A new MongoDB client
     */
    public static MongoClient createClient(Config config) {
        logger.info("Creating MongoDB client with URI: {}", config.getMongoUri());
        
        ConnectionString connectionString = new ConnectionString(config.getMongoUri());
        
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(30, TimeUnit.SECONDS)
                           .readTimeout(30, TimeUnit.SECONDS))
                .applyToConnectionPoolSettings(builder ->
                    builder.maxSize(config.getMongoMaxPoolSize())
                           .minSize(config.getMongoMinPoolSize())
                           .maxWaitTime(30, TimeUnit.SECONDS))
                .build();
        
        return MongoClients.create(settings);
    }
}
