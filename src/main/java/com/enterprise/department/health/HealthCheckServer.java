package com.enterprise.department.health;

import com.enterprise.department.config.Config;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple HTTP server for health checks.
 */
public class HealthCheckServer {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckServer.class);
    
    private final int port;
    private final AtomicBoolean healthy = new AtomicBoolean(true);
    private final HealthCheckService healthCheckService;
    private HttpServer server;
    
    /**
     * Create a new health check server.
     * 
     * @param port The port to listen on
     */
    public HealthCheckServer(int port) {
        this.port = port;
        this.healthCheckService = null;
    }
    
    /**
     * Create a new health check server with a health check service.
     * 
     * @param config The application configuration
     * @param healthCheckService The health check service
     */
    public HealthCheckServer(Config config, HealthCheckService healthCheckService) {
        this.port = config.getHealthPort();
        this.healthCheckService = healthCheckService;
    }
    
    /**
     * Start the health check server.
     */
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/health", new HealthHandler());
            server.createContext("/ready", new ReadyHandler());
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();
            logger.info("Health check server started on port {}", port);
        } catch (IOException e) {
            logger.error("Failed to start health check server", e);
        }
    }
    
    /**
     * Stop the health check server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            logger.info("Health check server stopped");
        }
    }
    
    /**
     * Set the health status of the application.
     * 
     * @param healthy true if the application is healthy, false otherwise
     */
    public void setHealthStatus(boolean healthy) {
        this.healthy.set(healthy);
        logger.info("Health status set to: {}", healthy);
    }
    
    /**
     * Handler for /health endpoint.
     */
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            boolean isHealthy = healthCheckService != null ? 
                    healthCheckService.checkHealth().isHealthy() : 
                    healthy.get();
            
            String response = "{\"status\":\"" + (isHealthy ? "UP" : "DOWN") + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(isHealthy ? 200 : 503, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    /**
     * Handler for /ready endpoint.
     */
    private class ReadyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            boolean isReady = healthCheckService != null ? 
                    healthCheckService.isReady() : 
                    true;
            
            String response = "{\"status\":\"" + (isReady ? "READY" : "NOT_READY") + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(isReady ? 200 : 503, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
