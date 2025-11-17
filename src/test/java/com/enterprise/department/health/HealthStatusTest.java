package com.enterprise.department.health;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthStatusTest {

    @Test
    void constructor_ShouldSetFields() {
        // Given
        Map<String, ComponentHealth> components = new HashMap<>();
        components.put("component1", new ComponentHealth(true, "Healthy component"));
        components.put("component2", new ComponentHealth(false, "Unhealthy component"));
        
        // When
        HealthStatus status = new HealthStatus(true, components, "Test metrics");
        
        // Then
        assertTrue(status.isHealthy());
        assertEquals(components, status.getComponents());
        assertEquals("Test metrics", status.getMetrics());
    }
    
    @Test
    void constants_ShouldHaveCorrectValues() {
        // Then
        assertTrue(HealthStatus.UP.isHealthy());
        assertFalse(HealthStatus.DOWN.isHealthy());
        assertFalse(HealthStatus.UNKNOWN.isHealthy());
        
        assertEquals("Status unknown", HealthStatus.UNKNOWN.getMetrics());
    }
    
    @Test
    void valueOf_WithValidValues_ShouldReturnCorrectStatus() {
        // Then
        assertEquals(HealthStatus.UP, HealthStatus.valueOf("UP"));
        assertEquals(HealthStatus.DOWN, HealthStatus.valueOf("DOWN"));
        assertEquals(HealthStatus.UNKNOWN, HealthStatus.valueOf("UNKNOWN"));
    }
    
    @Test
    void valueOf_WithInvalidValue_ShouldReturnUnknown() {
        // Then
        assertEquals(HealthStatus.UNKNOWN, HealthStatus.valueOf("INVALID"));
    }
    
    @Test
    void valueOf_WithNullValue_ShouldReturnUnknown() {
        // Then
        assertEquals(HealthStatus.UNKNOWN, HealthStatus.valueOf(null));
    }
    
    @Test
    void values_ShouldReturnAllValues() {
        // When
        HealthStatus[] values = HealthStatus.values();
        
        // Then
        assertEquals(3, values.length);
        assertTrue(containsStatus(values, HealthStatus.UP));
        assertTrue(containsStatus(values, HealthStatus.DOWN));
        assertTrue(containsStatus(values, HealthStatus.UNKNOWN));
    }
    
    private boolean containsStatus(HealthStatus[] values, HealthStatus status) {
        for (HealthStatus value : values) {
            if (value == status) {
                return true;
            }
        }
        return false;
    }
}
