package com.enterprise.department.core.shutdown;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages graceful shutdown of application components.
 * Executes shutdown tasks in parallel and waits for their completion.
 */
@Slf4j
public class GracefulShutdown {
    private final List<ShutdownTask> shutdownTasks = new ArrayList<>();
    
    /**
     * Add a shutdown task.
     *
     * @param task The task to execute during shutdown
     * @param taskName A descriptive name for the task
     */
    public void addShutdownTask(Runnable task, String taskName) {
        shutdownTasks.add(new ShutdownTask(task, taskName));
        log.debug("Added shutdown task: {}", taskName);
    }
    
    /**
     * Execute all shutdown tasks and wait for their completion.
     *
     * @param timeoutSeconds Maximum time to wait for tasks to complete
     * @return true if all tasks completed successfully, false otherwise
     */
    public boolean waitForCompletion(long timeoutSeconds) {
        if (shutdownTasks.isEmpty()) {
            log.info("No shutdown tasks to execute");
            return true;
        }
        
        log.info("Executing {} shutdown tasks with timeout of {}s", shutdownTasks.size(), timeoutSeconds);
        
        // Convert tasks to CompletableFutures and execute them
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (ShutdownTask task : shutdownTasks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    log.info("Executing shutdown task: {}", task.getName());
                    task.getTask().run();
                    log.info("Completed shutdown task: {}", task.getName());
                } catch (Exception e) {
                    log.error("Error during shutdown task: {}", task.getName(), e);
                    throw e;
                }
            });
            futures.add(future);
        }
        
        // Wait for all tasks to complete
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        
        try {
            allTasks.get(timeoutSeconds, TimeUnit.SECONDS);
            log.info("All shutdown tasks completed successfully");
            return true;
        } catch (Exception e) {
            log.warn("Not all shutdown tasks completed within timeout of {}s: {}", 
                    timeoutSeconds, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the number of registered shutdown tasks.
     *
     * @return The number of tasks
     */
    public int getTaskCount() {
        return shutdownTasks.size();
    }
    
    /**
     * Represents a task to be executed during shutdown.
     */
    private static class ShutdownTask {
        private final Runnable task;
        private final String name;
        
        public ShutdownTask(Runnable task, String name) {
            this.task = task;
            this.name = name;
        }
        
        public Runnable getTask() {
            return task;
        }
        
        public String getName() {
            return name;
        }
    }
}
