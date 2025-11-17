# Resilience Features in MongoDB to Kafka CDC

This document details the resilience features implemented in the MongoDB to Kafka CDC application to ensure reliable operation even in the face of failures and unexpected conditions.

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
    
    @Getter
    private State state = State.CLOSED;
    
    public enum State { CLOSED, OPEN, HALF_OPEN }
    
    public CircuitBreaker(String name, int failureThreshold, long resetTimeoutMs) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
    }
    
    public <T> T execute(Supplier<T> action) throws Exception {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > resetTimeoutMs) {
                state = State.HALF_OPEN;
                log.warn("Circuit {} half-open, attempting reset", name);
            } else {
                throw new CircuitBreakerOpenException("Circuit " + name + " is open");
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
        
        if (failureCount >= failureThreshold && state == State.CLOSED) {
            state = State.OPEN;
            log.warn("Circuit {} opened after {} failures", name, failureCount);
        }
    }
    
    private void reset() {
        failureCount = 0;
        state = State.CLOSED;
        log.info("Circuit {} reset to closed state", name);
    }
}
```

### Usage

```java
// In InitialLoader
public void load() throws Exception {
    try {
        // Use circuit breaker to protect against MongoDB failures
        mongoCircuitBreaker.execute(() -> {
            // MongoDB operations
            return null;
        });
    } catch (CircuitBreaker.CircuitBreakerOpenException e) {
        log.error("Initial load failed: MongoDB circuit breaker is open", e);
        throw e;
    }
}
```

### Benefits

1. **Failure isolation**: Prevents cascading failures across the system
2. **Fast failure**: Fails fast when a service is unavailable
3. **Self-healing**: Automatically attempts recovery after a timeout
4. **Monitoring**: Provides insights into system health

## Retry Mechanisms

Retry mechanisms help handle transient failures by automatically retrying operations that might succeed on subsequent attempts.

### Implementation

```java
// In KafkaFactory
public static KafkaProducer<String, String> createProducer(Config config) {
    Properties props = new Properties();
    // Basic configuration
    
    // Add retry settings
    props.put(ProducerConfig.RETRIES_CONFIG, config.getRetryMaxAttempts());
    props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, config.getRetryBackoffMs());
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    
    return new KafkaProducer<>(props);
}
```

### Benefits

1. **Resilience to transient failures**: Automatically recovers from temporary issues
2. **Configurable retry policy**: Adjustable number of attempts and backoff time
3. **Idempotence**: Ensures operations are applied exactly once
4. **Reduced manual intervention**: Handles recoverable errors automatically

## Resume Capability

The resume capability allows the application to continue processing from where it left off after a restart or failure.

### Implementation

```java
// In ChangeStreamProcessor
public void processChanges() throws Exception {
    try {
        mongoCircuitBreaker.execute(() -> {
            MongoDatabase database = mongoClient.getDatabase(config.getMongodbDatabase());
            MongoCollection<Document> collection = database.getCollection(config.getMongodbCollection());
            
            // Load the resume token if available
            Optional<BsonDocument> resumeToken = Optional.ofNullable(tokenManager.loadResumeToken());
            
            // Create the change stream
            MongoCursor<ChangeStreamDocument<Document>> cursor;
            if (resumeToken.isPresent()) {
                log.warn("Resuming change stream from token");
                cursor = collection.watch(createPipeline())
                        .fullDocument(FullDocument.UPDATE_LOOKUP)
                        .resumeAfter(resumeToken.get())
                        .iterator();
            } else {
                log.warn("Starting new change stream");
                cursor = collection.watch(createPipeline())
                        .fullDocument(FullDocument.UPDATE_LOOKUP)
                        .iterator();
            }
            
            // Process events and save resume token
            while (running.get()) {
                if (cursor.hasNext()) {
                    ChangeStreamDocument<Document> changeEvent = cursor.next();
                    processEvent(changeEvent);
                    tokenManager.saveResumeToken(changeEvent.getResumeToken());
                }
            }
            
            return null;
        });
    } catch (Exception e) {
        log.error("Error in change stream", e);
        throw e;
    }
}
```

### Benefits

1. **Durability**: No data loss on application restart
2. **Efficiency**: Avoids reprocessing already processed data
3. **Reliability**: Ensures exactly-once processing semantics
4. **Flexibility**: Works in both local and cloud environments

## Graceful Shutdown

Graceful shutdown ensures that all resources are properly released and in-flight operations are completed before the application exits.

### Implementation

```java
@Slf4j
public class GracefulShutdown {
    private final List<ShutdownTask> shutdownTasks = new ArrayList<>();
    
    public void addShutdownTask(Runnable task, String taskName) {
        shutdownTasks.add(new ShutdownTask(task, taskName));
        log.debug("Added shutdown task: {}", taskName);
    }
    
    public boolean waitForCompletion(long timeoutSeconds) {
        if (shutdownTasks.isEmpty()) {
            return true;
        }
        
        log.info("Executing {} shutdown tasks with timeout of {}s", 
                shutdownTasks.size(), timeoutSeconds);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (ShutdownTask task : shutdownTasks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    log.info("Executing shutdown task: {}", task.getName());
                    task.getTask().run();
                    log.info("Completed shutdown task: {}", task.getName());
                } catch (Exception e) {
                    log.error("Error during shutdown task: {}", task.getName(), e);
                    throw e;
                }
            });
            futures.add(future);
        }
        
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        
        try {
            allTasks.get(timeoutSeconds, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.warn("Not all shutdown tasks completed within timeout", e);
            return false;
        }
    }
}
```

### Usage

```java
// In ApplicationContext
private void setupShutdownHooks() {
    gracefulShutdown.addShutdownTask(() -> {
        healthServer.setHealthStatus(false);
        healthServer.stop();
    }, "HealthServer");
    
    gracefulShutdown.addShutdownTask(() -> {
        batchKafkaProducer.flush();
        kafkaProducer.close();
    }, "KafkaProducer");
    
    gracefulShutdown.addShutdownTask(() -> {
        MongoConnectionPool.closeClient();
    }, "MongoClient");
    
    // Add JVM shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        log.warn("JVM shutdown hook triggered");
        close();
    }));
}
```

### Benefits

1. **Resource cleanup**: Ensures all resources are properly released
2. **Data integrity**: Completes in-flight operations before shutdown
3. **Parallel shutdown**: Executes shutdown tasks concurrently for efficiency
4. **Timeout handling**: Prevents hanging during shutdown

## Error Handling

Comprehensive error handling ensures that the application can recover from or gracefully handle various types of errors.

### Implementation

```java
// In ChangeStreamSubject
public void notifyObservers(ChangeStreamDocument<Document> event) {
    String operationType = event.getOperationType().getValue();
    log.debug("Notifying {} observers of {} event", observers.size(), operationType);
    
    observers.forEach(observer -> {
        try {
            observer.onEvent(event);
        } catch (Exception e) {
            log.error("Error in observer while processing {} event", operationType, e);
            // Continue with other observers even if one fails
        }
    });
}
```

### Benefits

1. **Fault isolation**: Errors in one component don't affect others
2. **Detailed logging**: Comprehensive error information for troubleshooting
3. **Graceful degradation**: Application continues functioning despite partial failures
4. **Observability**: Errors are logged for monitoring and alerting

## Health Checks

Health checks provide a way to monitor the application's health and readiness to handle requests.

### Implementation

```java
@Slf4j
public class HealthCheckServer {
    private final int port;
    private final AtomicBoolean healthy = new AtomicBoolean(true);
    private HttpServer server;
    
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/health", new HealthHandler());
            server.createContext("/ready", new ReadyHandler());
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();
            log.warn("Health check server started on port {}", port);
        } catch (IOException e) {
            log.error("Failed to start health check server", e);
        }
    }
    
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"" + (healthy.get() ? "UP" : "DOWN") + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(healthy.get() ? 200 : 503, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
```

### Benefits

1. **Kubernetes integration**: Enables health and readiness probes
2. **Automated recovery**: Allows orchestration systems to restart unhealthy instances
3. **Monitoring**: Provides endpoints for monitoring systems
4. **Operational visibility**: Exposes application health status

## Metrics Collection

Metrics collection provides insights into the application's performance and behavior, helping identify issues and optimize performance.

### Implementation

```java
@Slf4j
public class MetricsCollector {
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    
    public void incrementCounter(String name) {
        counters.computeIfAbsent(name, k -> new Counter()).increment();
    }
    
    public Timer startTimer(String name) {
        Timer timer = new Timer();
        timers.put(name, timer);
        return timer;
    }
    
    public void logMetrics() {
        log.info("=== Application Metrics ===");
        counters.forEach((name, counter) -> 
            log.info("  {} = {}", name, counter.getValue().get()));
        timers.forEach((name, timer) -> 
            log.info("  {} = {}ms", name, timer.getDurationMs()));
    }
}
```

### Benefits

1. **Performance insights**: Helps identify bottlenecks and optimization opportunities
2. **Operational visibility**: Provides data for monitoring and alerting
3. **Capacity planning**: Helps determine resource requirements
4. **Anomaly detection**: Enables detection of unusual patterns or behaviors

## Conclusion

These resilience features work together to create a robust, fault-tolerant application that can handle various failure scenarios:

1. **Circuit Breaker**: Prevents cascading failures
2. **Retry Mechanisms**: Handles transient failures
3. **Resume Capability**: Ensures no data loss on restart
4. **Graceful Shutdown**: Properly releases resources
5. **Error Handling**: Isolates and manages failures
6. **Health Checks**: Enables monitoring and automated recovery
7. **Metrics Collection**: Provides operational insights

By implementing these features, the MongoDB to Kafka CDC application can maintain reliable operation even in challenging environments with network issues, service outages, or other failures.
