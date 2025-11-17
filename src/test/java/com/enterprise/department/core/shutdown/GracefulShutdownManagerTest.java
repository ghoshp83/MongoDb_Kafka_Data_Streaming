package com.enterprise.department.core.shutdown;

import com.enterprise.department.core.metrics.MetricsCollector;
import com.enterprise.department.core.resilience.CircuitBreaker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GracefulShutdownManagerTest {

    @Mock
    private KafkaProducer<String, String> kafkaProducer;
    
    @Mock
    private MetricsCollector metricsCollector;
    
    @Mock
    private MetricsCollector.Timer timer;
    
    private GracefulShutdownManager shutdownManager;
    
    @BeforeEach
    void setUp() {
        shutdownManager = new GracefulShutdownManager(kafkaProducer, metricsCollector);
        when(metricsCollector.startTimer(anyString())).thenReturn(timer);
    }
    
    @Test
    void shouldRegisterAndExecuteStandardShutdownHooks() {
        // Arrange
        Runnable hook1 = mock(Runnable.class);
        Runnable hook2 = mock(Runnable.class);
        
        // Act
        shutdownManager.registerShutdownHook(hook1);
        shutdownManager.registerShutdownHook(hook2);
        shutdownManager.initiateShutdown();
        
        // Assert
        verify(hook1).run();
        verify(hook2).run();
        verify(kafkaProducer).flush();
        verify(kafkaProducer).close();
    }
    
    @Test
    void shouldRegisterAndExecuteCircuitBreakerShutdownHooks() throws Exception {
        // Arrange
        CircuitBreaker.RunnableWithException hook1 = mock(CircuitBreaker.RunnableWithException.class);
        CircuitBreaker.RunnableWithException hook2 = mock(CircuitBreaker.RunnableWithException.class);
        
        // Act
        shutdownManager.registerShutdownHook(hook1);
        shutdownManager.registerShutdownHook(hook2);
        shutdownManager.initiateShutdown();
        
        // Assert
        verify(hook1).run();
        verify(hook2).run();
        verify(kafkaProducer).flush();
        verify(kafkaProducer).close();
    }
    
    @Test
    void shouldWaitForTasksToComplete() {
        // Arrange
        CompletableFuture<Void> future1 = new CompletableFuture<>();
        CompletableFuture<Void> future2 = new CompletableFuture<>();
        
        // Act
        shutdownManager.registerTask(future1);
        shutdownManager.registerTask(future2);
        
        // Complete tasks
        future1.complete(null);
        future2.complete(null);
        
        // Wait for completion
        boolean completed = shutdownManager.waitForCompletion(1);
        
        // Assert
        assertThat(completed).isTrue();
    }
    
    @Test
    void shouldTimeoutWhenTasksDoNotComplete() {
        // Arrange
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // Act
        shutdownManager.registerTask(future);
        boolean completed = shutdownManager.waitForCompletion(1);
        
        // Assert
        assertThat(completed).isFalse();
    }
    
    @Test
    void shouldHandleExceptionsDuringShutdown() throws Exception {
        // Arrange
        CircuitBreaker.RunnableWithException failingHook = () -> {
            throw new RuntimeException("Test exception");
        };
        
        // Act
        shutdownManager.registerShutdownHook(failingHook);
        shutdownManager.initiateShutdown();
        
        // Assert - should not throw exception
        verify(kafkaProducer).flush();
        verify(kafkaProducer).close();
    }
    
    @Test
    void waitForGracefulShutdown_shouldDelegateToWaitForCompletion() {
        // Arrange
        CompletableFuture<Void> task = new CompletableFuture<>();
        shutdownManager.registerTask(task);
        
        // Act & Assert
        assertThat(shutdownManager.waitForGracefulShutdown(1)).isFalse();
        
        // Complete task
        task.complete(null);
        
        // Now should complete
        assertThat(shutdownManager.waitForGracefulShutdown(1)).isTrue();
    }
}
