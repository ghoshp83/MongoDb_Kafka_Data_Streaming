package com.enterprise.department.health;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentHealthTest {

    @Test
    void constructor_WithHealthyAndMessage_ShouldSetFields() {
        // When
        ComponentHealth health = new ComponentHealth(true, "Healthy component");
        
        // Then
        assertTrue(health.isHealthy());
        assertEquals("Healthy component", health.getMessage());
        assertEquals("", health.getName());
        assertEquals(HealthStatus.UP, health.getStatus());
        assertEquals("Healthy component", health.getDetails());
    }
    
    @Test
    void constructor_WithUnhealthyAndMessage_ShouldSetFields() {
        // When
        ComponentHealth health = new ComponentHealth(false, "Unhealthy component");
        
        // Then
        assertFalse(health.isHealthy());
        assertEquals("Unhealthy component", health.getMessage());
        assertEquals("", health.getName());
        assertEquals(HealthStatus.DOWN, health.getStatus());
        assertEquals("Unhealthy component", health.getDetails());
    }
    
    @Test
    void constructor_WithNameStatusAndDetails_ShouldSetFields() {
        // When
        ComponentHealth health = new ComponentHealth("test-component", HealthStatus.UP, "Component details");
        
        // Then
        assertTrue(health.isHealthy());
        assertEquals("Component details", health.getMessage());
        assertEquals("test-component", health.getName());
        assertEquals(HealthStatus.UP, health.getStatus());
        assertEquals("Component details", health.getDetails());
    }
    
    @Test
    void setDetails_ShouldUpdateDetails() {
        // Given
        ComponentHealth health = new ComponentHealth(true, "Initial details");
        
        // When
        health.setDetails("Updated details");
        
        // Then
        assertEquals("Updated details", health.getDetails());
    }
}
