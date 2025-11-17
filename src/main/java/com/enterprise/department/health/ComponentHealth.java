package com.enterprise.department.health;

/**
 * Represents the health status of a component.
 */
public class ComponentHealth {
    private final boolean healthy;
    private final String message;
    private final String name;
    private HealthStatus status;
    private String details;
    
    /**
     * Creates a new ComponentHealth.
     *
     * @param healthy Whether the component is healthy
     * @param message A message describing the health status
     */
    public ComponentHealth(boolean healthy, String message) {
        this.healthy = healthy;
        this.message = message;
        this.name = "";
        this.status = healthy ? HealthStatus.UP : HealthStatus.DOWN;
        this.details = message;
    }
    
    /**
     * Creates a new ComponentHealth with name, status, and details.
     *
     * @param name The name of the component
     * @param status The health status
     * @param details Additional details about the health status
     */
    public ComponentHealth(String name, HealthStatus status, String details) {
        this.healthy = status == HealthStatus.UP;
        this.message = details;
        this.name = name;
        this.status = status;
        this.details = details;
    }
    
    /**
     * Checks if the component is healthy.
     *
     * @return True if the component is healthy, false otherwise
     */
    public boolean isHealthy() {
        return healthy;
    }
    
    /**
     * Gets the health status message.
     *
     * @return The health status message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the name of the component.
     *
     * @return The name of the component
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the health status.
     *
     * @return The health status
     */
    public HealthStatus getStatus() {
        return status;
    }
    
    /**
     * Gets additional details about the health status.
     *
     * @return Additional details about the health status
     */
    public String getDetails() {
        return details;
    }
    
    /**
     * Sets additional details about the health status.
     *
     * @param details Additional details about the health status
     */
    public void setDetails(String details) {
        this.details = details;
    }
}
