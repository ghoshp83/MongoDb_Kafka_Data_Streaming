package com.enterprise.department.health;

import java.util.Map;

/**
 * Represents the overall health status of the application.
 */
public class HealthStatus {
    // Health status constants
    public static final HealthStatus UP = new HealthStatus(true, Map.of(), "");
    public static final HealthStatus DOWN = new HealthStatus(false, Map.of(), "");
    public static final HealthStatus UNKNOWN = new HealthStatus(false, Map.of(), "Status unknown");
    
    private final boolean healthy;
    private final Map<String, ComponentHealth> components;
    private final String metrics;
    
    /**
     * Creates a new HealthStatus.
     *
     * @param healthy Whether the application is healthy
     * @param components The health status of each component
     * @param metrics The metrics report
     */
    public HealthStatus(boolean healthy, Map<String, ComponentHealth> components, String metrics) {
        this.healthy = healthy;
        this.components = components;
        this.metrics = metrics;
    }
    
    /**
     * Checks if the application is healthy.
     *
     * @return True if the application is healthy, false otherwise
     */
    public boolean isHealthy() {
        return healthy;
    }
    
    /**
     * Gets the health status of each component.
     *
     * @return A map of component names to health statuses
     */
    public Map<String, ComponentHealth> getComponents() {
        return components;
    }
    
    /**
     * Gets the metrics report.
     *
     * @return The metrics report
     */
    public String getMetrics() {
        return metrics;
    }
    
    /**
     * Gets a HealthStatus enum value from a string.
     *
     * @param status The status string
     * @return The corresponding HealthStatus enum value
     */
    public static HealthStatus valueOf(String status) {
        if (status == null) {
            return UNKNOWN;
        }
        
        switch (status.toUpperCase()) {
            case "UP":
                return UP;
            case "DOWN":
                return DOWN;
            default:
                return UNKNOWN;
        }
    }
    
    /**
     * Gets all possible HealthStatus values.
     *
     * @return An array of all HealthStatus values
     */
    public static HealthStatus[] values() {
        return new HealthStatus[] { UP, DOWN, UNKNOWN };
    }
}
