package com.github.ghoshp83.mongokafkastream;

import com.github.ghoshp83.mongokafkastream.config.Config;
import com.github.ghoshp83.mongokafkastream.config.ConfigLoader;
import com.github.ghoshp83.mongokafkastream.core.mongo.ChangeStreamProcessor;
import com.github.ghoshp83.mongokafkastream.core.mongo.InitialLoader;
import com.github.ghoshp83.mongokafkastream.core.shutdown.GracefulShutdownManager;
import com.github.ghoshp83.mongokafkastream.health.HealthCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnterpriseDataIngestTest {

    @BeforeEach
    void setUp() {
        // Set the env property to test to prevent System.exit() calls
        System.setProperty("env", "test");
    }

    @Test
    void main_ShouldHandleExceptions() {
        // Arrange
        String[] args = new String[]{"test.properties"};
        
        // Mock static ConfigLoader to throw an exception
        try (MockedStatic<ConfigLoader> mockedConfigLoader = Mockito.mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(() -> ConfigLoader.loadConfig(args))
                    .thenThrow(new RuntimeException("Test exception"));
            
            // Act - this should not throw an exception
            EnterpriseDataIngest.main(args);
            
            // No assertions needed - we're just verifying it doesn't crash
        }
    }
}
