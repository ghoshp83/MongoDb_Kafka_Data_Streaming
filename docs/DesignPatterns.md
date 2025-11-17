# Design Patterns in MongoDB to Kafka CDC

This document details the design patterns used in the MongoDB to Kafka CDC application and explains how they contribute to the application's maintainability, extensibility, and performance.

## Strategy Pattern

The Strategy pattern allows us to define a family of algorithms, encapsulate each one, and make them interchangeable. In our application, we use this pattern for document processing.

### Implementation

```java
// Interface defining the strategy
@FunctionalInterface
public interface DocumentProcessingStrategy {
    void processDocument(Document document, String operation, String source);
}

// Concrete implementation for Kafka
@Slf4j
@RequiredArgsConstructor
public class KafkaDocumentProcessor implements DocumentProcessingStrategy {
    private final KafkaProducer<String, String> kafkaProducer;
    private final String topic;
    private final BatchKafkaProducer batchProducer;
    private final boolean useBatch;
    
    @Override
    public void processDocument(Document document, String operation, String source) {
        // Implementation details
    }
}
```

### Benefits

1. **Flexibility**: We can easily swap processing strategies without modifying existing code
2. **Testability**: Each strategy can be tested in isolation
3. **Extensibility**: New strategies can be added without changing client code
4. **Separation of concerns**: Processing logic is separated from document retrieval

## Observer Pattern

The Observer pattern establishes a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically. We use this pattern for change stream events.

### Implementation

```java
// Observer interface
@FunctionalInterface
public interface ChangeEventObserver {
    void onEvent(ChangeStreamDocument<Document> event);
}

// Subject that manages observers
@Slf4j
public class ChangeStreamSubject {
    private final List<ChangeEventObserver> observers = new CopyOnWriteArrayList<>();
    
    public void addObserver(ChangeEventObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(ChangeEventObserver observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(ChangeStreamDocument<Document> event) {
        observers.forEach(observer -> {
            try {
                observer.onEvent(event);
            } catch (Exception e) {
                log.error("Error in observer", e);
            }
        });
    }
}

// Concrete observer
@Slf4j
@RequiredArgsConstructor
public class StreamProcessor implements ChangeEventObserver {
    private final DocumentProcessingStrategy documentProcessor;
    private final Config config;
    
    @Override
    public void onEvent(ChangeStreamDocument<Document> event) {
        // Process the event
    }
}
```

### Benefits

1. **Loose coupling**: The subject doesn't need to know anything about its observers
2. **Broadcast communication**: Multiple components can react to the same event
3. **Dynamic relationships**: Observers can be added or removed at runtime
4. **Extensibility**: New observers can be added without modifying the subject

## Factory Pattern

The Factory pattern provides an interface for creating objects without specifying their concrete classes. We use this pattern for creating components with complex initialization logic.

### Implementation

```java
@Slf4j
public class ResumeTokenManagerFactory {
    public static ResumeTokenManager createResumeTokenManager(Config config) {
        // Check if AWS credentials are available
        String awsAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String awsSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        if (awsAccessKey != null && !awsAccessKey.isEmpty() && 
            awsSecretKey != null && !awsSecretKey.isEmpty()) {
            return new S3ResumeTokenManager(config);
        } else {
            return new LocalResumeTokenManager(config);
        }
    }
}
```

### Benefits

1. **Encapsulation**: Creation logic is encapsulated in one place
2. **Flexibility**: The factory can return different implementations based on conditions
3. **Abstraction**: Clients work with interfaces, not concrete implementations
4. **Centralization**: Creation logic is centralized, making it easier to maintain

## Singleton Pattern

The Singleton pattern ensures a class has only one instance and provides a global point of access to it. We use this pattern for shared resources like connection pools.

### Implementation

```java
@Slf4j
public class MongoConnectionPool {
    private static volatile MongoClient sharedClient;
    private static final Object LOCK = new Object();
    
    private MongoConnectionPool() {
        // Private constructor to prevent instantiation
    }
    
    public static MongoClient getClient(Config config) {
        if (sharedClient == null) {
            synchronized (LOCK) {
                if (sharedClient == null) {
                    sharedClient = createClient(config);
                }
            }
        }
        return sharedClient;
    }
    
    private static MongoClient createClient(Config config) {
        // Create and configure MongoDB client
    }
    
    public static void closeClient() {
        synchronized (LOCK) {
            if (sharedClient != null) {
                sharedClient.close();
                sharedClient = null;
            }
        }
    }
}
```

### Benefits

1. **Resource sharing**: Ensures only one instance of resource-intensive objects
2. **Global access**: Provides a single access point to the shared resource
3. **Lazy initialization**: The instance is created only when needed
4. **Thread safety**: Implementation ensures thread-safe initialization

## Circuit Breaker Pattern

The Circuit Breaker pattern prevents cascading failures by detecting failures and encapsulating the logic of preventing a failure from constantly recurring.

### Implementation

```java
@Slf4j
public class CircuitBreaker {
    private final String name;
    private final int failureThreshold;
    private final long resetTimeoutMs;
    
    private int failureCount = 0;
    private long lastFailureTime = 0;
    private State state = State.CLOSED;
    
    public enum State { CLOSED, OPEN, HALF_OPEN }
    
    public <T> T execute(Supplier<T> action) throws Exception {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > resetTimeoutMs) {
                state = State.HALF_OPEN;
            } else {
                throw new CircuitBreakerOpenException("Circuit is open");
            }
        }
        
        try {
            T result = action.get();
            if (state == State.HALF_OPEN) {
                reset();
            }
            return result;
        } catch (Exception e) {
            recordFailure();
            throw e;
        }
    }
    
    private void recordFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
        }
    }
    
    private void reset() {
        failureCount = 0;
        state = State.CLOSED;
    }
}
```

### Benefits

1. **Failure isolation**: Prevents cascading failures across the system
2. **Fast failure**: Fails fast when a service is unavailable
3. **Resilience**: Automatically attempts recovery after a timeout
4. **Monitoring**: Provides insights into system health

## Dependency Injection

Dependency Injection is a technique where an object receives other objects it depends on. We use this pattern to manage component dependencies and improve testability.

### Implementation

```java
@Slf4j
@Getter
public class ApplicationContext implements AutoCloseable {
    // Configuration
    private final Config config;
    
    // Core components
    private final MongoClient mongoClient;
    private final KafkaProducer<String, String> kafkaProducer;
    private final ResumeTokenManager tokenManager;
    private final BatchKafkaProducer batchKafkaProducer;
    private final DocumentProcessingStrategy documentProcessor;
    private final BatchDocumentProcessor batchDocumentProcessor;
    private final ChangeStreamSubject changeStreamSubject;
    private final StreamProcessor streamProcessor;
    
    // Support components
    private final MetricsCollector metricsCollector;
    private final CircuitBreaker mongoCircuitBreaker;
    private final CircuitBreaker kafkaCircuitBreaker;
    private final GracefulShutdown gracefulShutdown;
    private final HealthCheckServer healthServer;
    
    public ApplicationContext(Config config) {
        this.config = config;
        
        // Initialize components
        this.metricsCollector = new MetricsCollector();
        this.mongoCircuitBreaker = new CircuitBreaker("MongoDB", 3, 30000);
        this.kafkaCircuitBreaker = new CircuitBreaker("Kafka", 3, 10000);
        this.mongoClient = MongoConnectionPool.getClient(config);
        this.kafkaProducer = KafkaFactory.createProducer(config);
        this.tokenManager = ResumeTokenManagerFactory.createResumeTokenManager(config);
        
        // Initialize other components
    }
}
```

### Benefits

1. **Loose coupling**: Components are not responsible for creating their dependencies
2. **Testability**: Dependencies can be easily mocked for testing
3. **Flexibility**: Dependencies can be swapped at runtime
4. **Lifecycle management**: Central control over component lifecycle

## Conclusion

By applying these design patterns, we've created a modular, extensible, and maintainable application. Each pattern addresses specific concerns:

- **Strategy Pattern**: Flexible document processing
- **Observer Pattern**: Decoupled event handling
- **Factory Pattern**: Simplified component creation
- **Singleton Pattern**: Efficient resource sharing
- **Circuit Breaker Pattern**: Improved resilience
- **Dependency Injection**: Better component management

Together, these patterns form a robust architecture that can handle the complexities of streaming data from MongoDB to Kafka in a reliable and efficient manner.
