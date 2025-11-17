package com.enterprise.department.api;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;

/**
 * Interface for observers of MongoDB change stream events.
 */
public interface ChangeEventObserver {
    
    /**
     * Called when a change event is received.
     *
     * @param event The change event
     */
    void onEvent(ChangeStreamDocument<Document> event);
}
