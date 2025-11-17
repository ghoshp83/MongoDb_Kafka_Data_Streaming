# Multi-stage build for MongoDB Kafka Data Streaming
FROM maven:3.9.6-eclipse-temurin-11 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first for better Docker layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:11-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1000 appuser && adduser -u 1000 -G appuser -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/enterprise-data-ingest-*-jar-with-dependencies.jar app.jar

# Create logs directory and set permissions
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose health check port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Default configuration
ENV MONGODB_URI="mongodb://localhost:27017" \
    MONGODB_DATABASE="mydb" \
    MONGODB_COLLECTION="mycollection" \
    KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
    KAFKA_TOPIC="mongodb-data" \
    HEALTH_PORT="8080" \
    INITIAL_LOAD_ENABLED="true" \
    AWS_REGION="us-east-1"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for better maintainability
LABEL maintainer="ghoshp83" \
      version="0.1.0" \
      description="MongoDB to Kafka Data Streaming Application" \
      org.opencontainers.image.source="https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming" \
      org.opencontainers.image.documentation="https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/blob/main/README.md" \
      org.opencontainers.image.licenses="MIT"