package com.enterprise.department.core.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    private MetricsCollector metricsCollector;

    @BeforeEach
    void setUp() {
        metricsCollector = new MetricsCollector();
    }

    @Test
    void incrementCounter_ShouldIncrementValue() {
        // When
        metricsCollector.incrementCounter("test.counter");
        metricsCollector.incrementCounter("test.counter");
        
        // Then
        assertEquals(2L, metricsCollector.getCounter("test.counter"));
    }

    @Test
    void startTimer_ShouldRecordDuration() throws InterruptedException {
        // When
        MetricsCollector.Timer timer = metricsCollector.startTimer("test.timer");
        Thread.sleep(10); // Sleep for a short time
        timer.stop();
        
        // Then
        assertTrue(metricsCollector.getTimer("test.timer") > 0);
        assertTrue(metricsCollector.getTimer("test.timer") >= 10);
    }

    @Test
    void getCounter_ShouldReturnZeroForNonExistentCounter() {
        // When/Then
        assertEquals(0L, metricsCollector.getCounter("non.existent.counter"));
    }

    @Test
    void getTimer_ShouldReturnZeroForNonExistentTimer() {
        // When/Then
        assertEquals(0L, metricsCollector.getTimer("non.existent.timer"));
    }

    @Test
    void getMetricsReport_ShouldIncludeAllMetrics() {
        // Given
        metricsCollector.incrementCounter("test.counter");
        MetricsCollector.Timer timer = metricsCollector.startTimer("test.timer");
        timer.stop();
        
        // When
        String report = metricsCollector.getMetricsReport();
        
        // Then
        assertTrue(report.contains("test.counter"));
        assertTrue(report.contains("test.timer"));
    }
}
