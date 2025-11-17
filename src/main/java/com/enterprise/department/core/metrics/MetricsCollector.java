package com.enterprise.department.core.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects metrics for monitoring.
 */
public class MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> timers = new ConcurrentHashMap<>();
    
    /**
     * Increments a counter.
     *
     * @param name The name of the counter
     */
    public void incrementCounter(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * Gets the value of a counter.
     *
     * @param name The name of the counter
     * @return The value of the counter, or 0 if the counter does not exist
     */
    public long getCounter(String name) {
        AtomicLong counter = counters.get(name);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * Starts a timer with the given name.
     *
     * @param name The name of the timer
     * @return A Timer object that can be used to stop the timer
     */
    public Timer startTimer(String name) {
        return new Timer(name, this);
    }
    
    /**
     * Records the duration of a timer.
     *
     * @param name The name of the timer
     * @param durationMs The duration in milliseconds
     */
    void recordTimer(String name, long durationMs) {
        timers.put(name, durationMs);
    }
    
    /**
     * Gets the duration of a timer.
     *
     * @param name The name of the timer
     * @return The duration in milliseconds, or 0 if the timer does not exist
     */
    public long getTimer(String name) {
        return timers.getOrDefault(name, 0L);
    }
    
    /**
     * Gets a report of all metrics.
     *
     * @return A string containing all metrics
     */
    public String getMetricsReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("Counters:\n");
        counters.forEach((name, value) -> report.append("  ").append(name).append(": ").append(value.get()).append("\n"));
        
        report.append("Timers:\n");
        timers.forEach((name, value) -> report.append("  ").append(name).append(": ").append(value).append(" ms\n"));
        
        return report.toString();
    }
    
    /**
     * Timer class for measuring durations.
     */
    public static class Timer {
        private final String name;
        private final MetricsCollector collector;
        private final long startTime;
        private boolean stopped = false;
        private long durationMs = 0;
        
        Timer(String name, MetricsCollector collector) {
            this.name = name;
            this.collector = collector;
            this.startTime = System.currentTimeMillis();
        }
        
        /**
         * Stops the timer and records the duration.
         */
        public void stop() {
            if (!stopped) {
                durationMs = System.currentTimeMillis() - startTime;
                collector.recordTimer(name, durationMs);
                stopped = true;
            }
        }
        
        /**
         * Gets the duration in milliseconds.
         * If the timer has not been stopped, returns the current elapsed time.
         *
         * @return The duration in milliseconds
         */
        public long getDurationMs() {
            if (stopped) {
                return durationMs;
            } else {
                return System.currentTimeMillis() - startTime;
            }
        }
    }
}
