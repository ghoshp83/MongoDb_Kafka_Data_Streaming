# Testing Strategy for MongoDB to Kafka CDC

This document outlines the comprehensive testing strategy for the MongoDB to Kafka CDC application, focusing on ensuring reliability, correctness, and performance.

## Testing Levels

### Unit Tests

Unit tests verify the behavior of individual components in isolation, using mocks for external dependencies.

#### Key Components to Test

1. **DocumentProcessingStrategy Implementations**
   ```java
   @Test
   void testKafkaDocumentProcessor() {
       // Arrange
       KafkaProducer<String, String> mockProducer = mock(KafkaProducer.class);
       String topic = "test-topic";
       KafkaDocumentProcessor processor = new KafkaDocumentProcessor(mockProducer, topic);
       Document document = new Document("_id", "123").append("name", "test");
       
       // Act
       processor.processDocument(document, "read", "test");
       
       // Assert
       verify(mockProducer).send(any(ProducerRecord.class));
   }
   ```

2. **CircuitBreaker**
   ```java
   @Test
   void testCircuitBreakerOpensAfterFailures() {
       // Arrange
       CircuitBreaker breaker = new CircuitBreaker("test", 3, 1000);
       AtomicInteger counter = new AtomicInteger(0);
       
       // Act & Assert
       // First 3 calls should fail but circuit stays closed
       for (int i = 0; i < 3; i++) {
           assertThrows(RuntimeException.class, () -> 
               breaker.execute(() -> { throw new RuntimeException("test"); }));
       }
       
       // Circuit should be open now
       assertThrows(CircuitBreaker.CircuitBreakerOpenException.class, () -> 
           breaker.execute(() -> counter.incrementAndGet()));
       
       // Counter should not have incremented
       assertEquals(0, counter.get());
   }
   ```

3. **BatchKafkaProducer**
   ```java
   @Test
   void testBatchKafkaProducerFlushesWhenBatchSizeMet() {
       // Arrange
       KafkaProducer<String, String> mockProducer = mock(KafkaProducer.class);
       int batchSize = 3;
       BatchKafkaProducer batchProducer = new BatchKafkaProducer(mockProducer, "test-topic", batchSize);
       
       // Act
       batchProducer.send("key1", "value1");
       batchProducer.send("key2", "value2");
       verify(mockProducer, never()).send(any(ProducerRecord.class));
       
       batchProducer.send("key3", "value3"); // This should trigger flush
       
       // Assert
       verify(mockProducer, times(3)).send(any(ProducerRecord.class));
   }
   ```

4. **ConfigLoader**
   ```java
   @Test
   void testConfigLoaderWithEnvironmentVariables() {
       // Arrange
       try (MockedStatic<System> systemMock = mockStatic(System.class)) {
           systemMock.when(() -> System.getenv("MONGODB_URI")).thenReturn("mongodb://testhost:27017");
           systemMock.when(() -> System.getenv("MONGODB_DATABASE")).thenReturn("testdb");
           
           // Act
           Config config = ConfigLoader.loadFromEnv();
           
           // Assert
           assertEquals("mongodb://testhost:27017", config.getMongodbUri());
           assertEquals("testdb", config.getMongodbDatabase());
       }
   }
   ```

### Integration Tests

Integration tests verify that different components work together correctly, using test doubles for external services.

#### Key Integration Points to Test

1. **MongoDB to Document Processor**
   ```java
   @Test
   void testInitialLoaderWithMockedMongo() {
       // Arrange
       MongoClient mockClient = mock(MongoClient.class);
       MongoDatabase mockDb = mock(MongoDatabase.class);
       MongoCollection<Document> mockCollection = mock(MongoCollection.class);
       MongoCursor<Document> mockCursor = mock(MongoCursor.class);
       
       when(mockClient.getDatabase(anyString())).thenReturn(mockDb);
       when(mockDb.getCollection(anyString())).thenReturn(mockCollection);
       when(mockCollection.find()).thenReturn(new FindIterable<Document>() {
           @Override
           public MongoCursor<Document> iterator() {
               return mockCursor;
           }
           // Other required overrides
       });
       
       when(mockCursor.hasNext()).thenReturn(true, true, false);
       when(mockCursor.next()).thenReturn(
           new Document("_id", "1").append("name", "test1"),
           new Document("_id", "2").append("name", "test2")
       );
       
       DocumentProcessingStrategy mockStrategy = mock(DocumentProcessingStrategy.class);
       BatchDocumentProcessor processor = new BatchDocumentProcessor(mockStrategy);
       
       // Act
       InitialLoader loader = new InitialLoader(mockClient, processor, config, metrics, circuitBreaker);
       loader.load();
       
       // Assert
       verify(mockStrategy, times(2)).processDocument(any(Document.class), eq("read"), eq("initial_load"));
   }
   ```

2. **Change Stream to Observer**
   ```java
   @Test
   void testChangeStreamProcessorNotifiesObservers() {
       // Arrange
       ChangeStreamSubject subject = new ChangeStreamSubject();
       ChangeEventObserver mockObserver = mock(ChangeEventObserver.class);
       subject.addObserver(mockObserver);
       
       ChangeStreamDocument<Document> mockEvent = mock(ChangeStreamDocument.class);
       when(mockEvent.getOperationType()).thenReturn(OperationType.INSERT);
       
       // Act
       subject.notifyObservers(mockEvent);
       
       // Assert
       verify(mockObserver).onEvent(mockEvent);
   }
   ```

### End-to-End Tests

End-to-end tests verify the complete pipeline using embedded or containerized services.

#### Test Setup

```java
@Test
void testEndToEndPipeline() {
    // Start embedded MongoDB
    MongodExecutable mongodExecutable = startEmbeddedMongo();
    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    
    // Start embedded Kafka
    EmbeddedKafkaCluster kafka = startEmbeddedKafka();
    
    try {
        // Set up test data
        MongoDatabase db = mongoClient.getDatabase("testdb");
        MongoCollection<Document> collection = db.getCollection("testcollection");
        collection.insertOne(new Document("_id", "test1").append("value", "original"));
        
        // Start the application
        Config config = Config.builder()
            .mongodbUri("mongodb://localhost:27017")
            .mongodbDatabase("testdb")
            .mongodbCollection("testcollection")
            .kafkaBootstrapServers("localhost:9092")
            .kafkaTopic("test-topic")
            .build();
        
        ApplicationContext context = new ApplicationContext(config);
        
        // Verify initial load
        KafkaConsumer<String, String> consumer = createConsumer(kafka);
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertEquals(1, records.count());
        
        // Test change data capture
        collection.updateOne(
            Filters.eq("_id", "test1"), 
            Updates.set("value", "updated")
        );
        
        // Verify CDC event
        records = consumer.poll(Duration.ofSeconds(5));
        assertEquals(1, records.count());
        assertTrue(records.iterator().next().value().contains("updated"));
        
    } finally {
        // Clean up
        kafka.stop();
        mongodExecutable.stop();
    }
}
```

## Test Categories

### Configuration Tests

Tests that verify the application correctly loads and parses configuration.

```java
@Test
void testConfigPrecedence() {
    // Test that command line args override classpath properties
    // Test that environment variables are used as fallback
}

@Test
void testDefaultValues() {
    // Test that default values are used when configuration is missing
}

@Test
void testInvalidConfiguration() {
    // Test error handling for invalid configuration
}
```

### MongoDB Source Tests

Tests that verify the MongoDB connection and event capture.

```java
@Test
void testMongoConnectionPooling() {
    // Test that connections are reused
}

@Test
void testChangeStreamEventCapture() {
    // Test that change stream events are captured correctly
}

@Test
void testMongoConnectionFailure() {
    // Test error handling for MongoDB connection issues
}
```

### Event Processing Tests

Tests that verify the processing of MongoDB events.

```java
@Test
void testDocumentConversion() {
    // Test conversion of MongoDB documents to JSON
}

@Test
void testSpecialDataTypes() {
    // Test handling of special MongoDB data types (ObjectId, Date, etc.)
}

@Test
void testKeyExtraction() {
    // Test extraction of keys from documents
}
```

### Kafka Sink Tests

Tests that verify the Kafka message production.

```java
@Test
void testKafkaMessageFormat() {
    // Test that messages are formatted correctly
}

@Test
void testKafkaConnectionFailure() {
    // Test error handling for Kafka connection issues
}

@Test
void testBatchProcessing() {
    // Test that messages are batched correctly
}
```

## Test Environment

### Unit Test Environment

- JUnit 5 for test execution
- Mockito for mocking dependencies
- AssertJ for assertions

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.8.2</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>4.5.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>4.5.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.22.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Integration Test Environment

- Embedded MongoDB for MongoDB tests
- Embedded Kafka for Kafka tests

```xml
<dependencies>
    <dependency>
        <groupId>de.flapdoodle.embed</groupId>
        <artifactId>de.flapdoodle.embed.mongo</artifactId>
        <version>3.4.5</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.3.1</version>
        <classifier>test</classifier>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka_2.13</artifactId>
        <version>3.3.1</version>
        <classifier>test</classifier>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Test Data

### Sample MongoDB Documents

```java
Document simpleDocument = new Document("_id", "simple")
    .append("name", "Simple Document")
    .append("value", 42)
    .append("active", true);

Document complexDocument = new Document("_id", "complex")
    .append("name", "Complex Document")
    .append("nested", new Document("key1", "value1").append("key2", "value2"))
    .append("array", Arrays.asList(1, 2, 3))
    .append("objectArray", Arrays.asList(
        new Document("id", 1).append("name", "Item 1"),
        new Document("id", 2).append("name", "Item 2")
    ));

Document specialTypesDocument = new Document("_id", new ObjectId())
    .append("date", new Date())
    .append("binary", new Binary("binary data".getBytes()))
    .append("decimal", new Decimal128(BigDecimal.valueOf(123.45)));
```

### Sample CDC Events

```java
// Insert event
ChangeStreamDocument<Document> insertEvent = createChangeStreamDocument(
    OperationType.INSERT,
    new Document("_id", "new").append("name", "New Document"),
    null
);

// Update event
ChangeStreamDocument<Document> updateEvent = createChangeStreamDocument(
    OperationType.UPDATE,
    new Document("_id", "existing").append("name", "Updated Document"),
    new Document("_id", "existing")
);

// Delete event
ChangeStreamDocument<Document> deleteEvent = createChangeStreamDocument(
    OperationType.DELETE,
    null,
    new Document("_id", "deleted")
);
```

## Test Execution

### Continuous Integration

- Run unit tests on every commit
- Run integration tests on pull requests and before releases

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    - name: Build with Maven
      run: mvn -B package --file pom.xml
    - name: Run unit tests
      run: mvn -B test
    - name: Run integration tests
      run: mvn -B verify -P integration-test
```

### Test Reports

- Generate test coverage reports with JaCoCo
- Generate test execution reports with Surefire

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>2.22.2</version>
        </plugin>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.7</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Conclusion

This comprehensive testing strategy ensures that the MongoDB to Kafka CDC application is thoroughly tested at all levels:

1. **Unit Tests**: Verify individual components in isolation
2. **Integration Tests**: Verify component interactions
3. **End-to-End Tests**: Verify the complete pipeline

By implementing this testing strategy, we can ensure that the application is reliable, performs well, and correctly handles all types of MongoDB documents and change events.
