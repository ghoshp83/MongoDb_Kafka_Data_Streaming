package com.enterprise.department.core.mongo;

import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a shared MongoDB client.
 */
public class MongoConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionPool.class);
    private static final Object LOCK = new Object();
    
    private static volatile MongoClient sharedClient;
    
    /**
     * Gets the shared MongoDB client.
     * If the client does not exist, it is created.
     *
     * @return The shared MongoDB client
     */
    public static MongoClient getClient() {
        if (sharedClient == null) {
            throw new IllegalStateException("MongoDB client has not been initialized");
        }
        return sharedClient;
    }
    
    /**
     * Sets the shared MongoDB client.
     *
     * @param client The MongoDB client
     */
    public static void setClient(MongoClient client) {
        synchronized (LOCK) {
            if (sharedClient != null) {
                logger.warn("Replacing existing MongoDB client");
                sharedClient.close();
            }
            sharedClient = client;
        }
    }
    
    /**
     * Closes the shared MongoDB client.
     */
    public static void closeClient() {
        synchronized (LOCK) {
            if (sharedClient != null) {
                logger.info("Closing shared MongoDB client");
                sharedClient.close();
                sharedClient = null;
            }
        }
    }
}
