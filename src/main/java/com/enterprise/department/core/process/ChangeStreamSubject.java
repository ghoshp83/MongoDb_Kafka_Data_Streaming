package com.enterprise.department.core.process;

import com.enterprise.department.api.ChangeEventObserver;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject in the Observer pattern for MongoDB change stream events.
 */
@Slf4j
public class ChangeStreamSubject {
    
    private final List<ChangeEventObserver> observers = new CopyOnWriteArrayList<>();
    
    /**
     * Add an observer to be notified of change events.
     *
     * @param observer The observer to add
     */
    public void addObserver(ChangeEventObserver observer) {
        observers.add(observer);
        log.debug("Added observer: {}", observer.getClass().getSimpleName());
    }
    
    /**
     * Remove an observer.
     *
     * @param observer The observer to remove
     */
    public void removeObserver(ChangeEventObserver observer) {
        observers.remove(observer);
        log.debug("Removed observer: {}", observer.getClass().getSimpleName());
    }
    
    /**
     * Notify all observers of a change event.
     *
     * @param event The change event
     */
    public void notifyObservers(ChangeStreamDocument<Document> event) {
        if (event == null) {
            log.warn("Received null event, not notifying observers");
            return;
        }
        
        String operationType = event.getOperationType().getValue();
        log.debug("Notifying {} observers of {} event", observers.size(), operationType);
        
        for (ChangeEventObserver observer : observers) {
            try {
                observer.onEvent(event);
            } catch (Exception e) {
                log.error("Error in observer while processing {} event", operationType, e);
                // Continue with other observers even if one fails
            }
        }
    }
}
