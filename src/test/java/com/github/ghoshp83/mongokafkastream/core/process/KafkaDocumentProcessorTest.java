package com.github.ghoshp83.mongokafkastream.core.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ghoshp83.mongokafkastream.core.kafka.BatchKafkaProducer;
import com.github.ghoshp83.mongokafkastream.core.metrics.MetricsCollector;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Covers the KafkaDocumentProcessor that ApplicationContext actually wires up.
 *
 * <p>This suite previously exercised an orphaned copy of this class in the
 * core.kafka package. That copy was never constructed by the application, so
 * the production processing path — the vuid key preference, the metadata
 * envelope added to every payload, and the documents.* metric names — was
 * untested. These tests pin the behaviour downstream Kafka consumers depend on.
 */
@ExtendWith(MockitoExtension.class)
class KafkaDocumentProcessorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private BatchKafkaProducer kafkaProducer;

    @Mock
    private MetricsCollector metricsCollector;

    private KafkaDocumentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new KafkaDocumentProcessor(kafkaProducer, metricsCollector);
    }

    @Test
    void processDocument_ShouldSendToKafkaAndCountByOperationAndSource() {
        Document document = new Document("_id", "test123")
                .append("name", "Test Document")
                .append("value", 42);

        processor.processDocument(document, "read", "initial_load");

        verify(kafkaProducer).send(eq("test123"), anyString());
        // Counted three ways so operators can break processing volume down by
        // operation and by source, not just in aggregate.
        verify(metricsCollector).incrementCounter("documents.processed");
        verify(metricsCollector).incrementCounter("documents.read");
        verify(metricsCollector).incrementCounter("documents.initial_load");
    }

    @Test
    void processDocument_WithVuid_ShouldUseVuidAsKey() {
        // vuid is the domain key; partitioning on it keeps all events for one
        // entity on the same partition, which is what preserves their ordering.
        Document document = new Document("_id", "test123")
                .append("vuid", "vehicle123")
                .append("name", "Test Document");

        processor.processDocument(document, "insert", "change_stream");

        verify(kafkaProducer).send(eq("vehicle123"), anyString());
    }

    @Test
    void processDocument_WithoutVuid_ShouldFallBackToIdAsKey() {
        Document document = new Document("_id", "test123")
                .append("name", "Test Document");

        processor.processDocument(document, "update", "change_stream");

        verify(kafkaProducer).send(eq("test123"), anyString());
    }

    @Test
    void processDocument_ShouldAddMetadataEnvelopeToPayload() throws Exception {
        Document document = new Document("_id", "test123").append("name", "Test Document");
        long before = System.currentTimeMillis();

        processor.processDocument(document, "insert", "change_stream");

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(kafkaProducer).send(eq("test123"), payload.capture());
        JsonNode sent = MAPPER.readTree(payload.getValue());

        // The metadata envelope is the contract consumers read to tell an
        // initial backfill apart from a live change-stream event.
        assertThat(sent.get("_operation").asText()).isEqualTo("insert");
        assertThat(sent.get("_source").asText()).isEqualTo("change_stream");
        assertThat(sent.get("_timestamp").asLong()).isBetween(before, System.currentTimeMillis());
        // Original document fields must survive the envelope.
        assertThat(sent.get("name").asText()).isEqualTo("Test Document");
    }

    @Test
    void processDocument_WithError_ShouldNotPropagateAndShouldCountError() {
        Document document = new Document("_id", "test123");
        doThrow(new RuntimeException("Test exception")).when(kafkaProducer).send(anyString(), anyString());

        // A single poison document must not kill the change-stream loop.
        processor.processDocument(document, "delete", "change_stream");

        verify(metricsCollector).incrementCounter("documents.errors");
    }

    @Test
    void close_ShouldFlushKafkaProducer() {
        processor.close();

        verify(kafkaProducer).flush();
    }
}
