# 🚀 MongoDB to Kafka Data Streaming

<div align="center">
  <img src="https://img.shields.io/github/stars/ghoshp83/MongoDb_Kafka_Data_Streaming?style=for-the-badge" alt="GitHub Stars">
  <img src="https://img.shields.io/github/forks/ghoshp83/MongoDb_Kafka_Data_Streaming?style=for-the-badge" alt="GitHub Forks">
  <img src="https://img.shields.io/github/issues/ghoshp83/MongoDb_Kafka_Data_Streaming?style=for-the-badge" alt="GitHub Issues">
  <img src="https://img.shields.io/github/license/ghoshp83/MongoDb_Kafka_Data_Streaming?style=for-the-badge" alt="License">
</div>

<div align="center">
  <img src="https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/workflows/CI%20Pipeline/badge.svg" alt="CI Pipeline">
  <img src="https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/workflows/Docker%20Build%20workflows/Docker%20Build%20workflows/Docker%20Build%20&%20Push/badge.svg%20Push/badge.svg%20Push/badge.svg" alt="Docker Build">
  <img src="https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/workflows/CodeQL%20Security%20Analysis/badge.svg" alt="CodeQL">
  <img src="https://codecov.io/gh/ghoshp83/MongoDb_Kafka_Data_Streaming/branch/main/graph/badge.svg" alt="Coverage">
</div>

<div align="center">
  <img src="https://img.shields.io/badge/Java-11+-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java 11+">
  <img src="https://img.shields.io/badge/MongoDB-5.0+-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB 5.0+">
  <img src="https://img.shields.io/badge/Apache_Kafka-2.0+-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white" alt="Apache Kafka 2.0+">
  <img src="https://img.shields.io/badge/AWS-S3_|_EKS-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white" alt="AWS S3 | EKS">
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker Ready">
  <img src="https://img.shields.io/badge/Kubernetes-Ready-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white" alt="Kubernetes Ready">
</div>

<p align="center">
  <strong>🔥 Enterprise-grade, high-performance data streaming solution</strong><br>
  Stream MongoDB documents to Kafka with real-time CDC, resumable processing, and production-ready resilience patterns
</p>

<div align="center">
  <a href="#-quick-start">🚀 Quick Start</a> •
  <a href="#-features">✨ Features</a> •
  <a href="#-architecture">🏗️ Architecture</a> •
  <a href="docs/DeploymentGuide.md">📖 Docs</a> •
  <a href="#-contributing">🤝 Contributing</a>
</div>

---

## 🚀 Quick Start

```bash
# 1. Clone and build
git clone https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming.git
cd MongoDb_Kafka_Data_Streaming
mvn clean package

# 2. Configure (set your MongoDB and Kafka endpoints)
export MONGODB_URI="mongodb://localhost:27017"
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
export MONGODB_DATABASE="mydb"
export MONGODB_COLLECTION="mycollection"
export KAFKA_TOPIC="mongodb-data"

# 3. Run
java -jar target/enterprise-data-ingest-0.1-jar-with-dependencies.jar
```

**🐳 Docker Quick Start:**
```bash
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017 \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  -e MONGODB_DATABASE=mydb \
  -e MONGODB_COLLECTION=mycollection \
  -e KAFKA_TOPIC=mongodb-data \
  ghoshp83/mongodb-kafka-streaming:latest
```

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Requirements](#-requirements)
- [Building the Application](#-building-the-application)
- [Configuration](#-configuration)
- [Running the Application](#-running-the-application)
- [How It Works](#-how-it-works)
- [Data Format](#-data-format)
- [Performance Optimizations](#-performance-optimizations)
- [Resilience Features](#-resilience-features)
- [Monitoring](#-monitoring)
- [Error Handling](#-error-handling)
- [Testing](#-testing)
- [Documentation](#-documentation)
- [Limitations](#-limitations)

<hr>

## 🔍 Overview

Enterprise Data Ingest is designed to efficiently stream data from MongoDB to Kafka with minimal overhead. It supports both initial data loading and real-time change data capture using MongoDB's Change Streams feature. The application is built with modern Java design patterns, focusing on performance, resilience, and maintainability.

## ✨ Features

- **Initial Data Loading**: Full snapshot of MongoDB collections to Kafka
- **Real-time CDC**: Capture and stream inserts, updates, and deletes as they happen
- **Resumable Processing**: Store and retrieve resume tokens for reliable operation
- **Environment Flexibility**: Run locally or in AWS without code changes
- **Horizontal Scaling**: Deploy multiple instances for increased throughput
- **Batch Processing**: Optimize network usage with configurable batch sizes
- **Resilience Patterns**: Circuit breakers, retry mechanisms, and graceful shutdown
- **Performance Optimizations**: Connection pooling, Kafka producer tuning, and more
- **Monitoring**: Health checks, metrics collection, and comprehensive logging
- **Kubernetes-Ready**: Designed for containerized deployment with health probes

## 🏗️ Architecture

The application follows a modular, component-based architecture with clear separation of concerns:

```
com.enterprise.department
├── api          # Public interfaces
├── config       # Configuration classes
├── core         # Core business logic
│   ├── kafka    # Kafka-related components
│   ├── mongo    # MongoDB-related components
│   ├── process  # Document processing logic
│   ├── resilience # Resilience patterns
│   ├── metrics  # Metrics collection
│   └── shutdown # Shutdown management
├── health       # Health check components
└── util         # Utility classes
```

### Design Patterns

The application implements several design patterns:

- **Strategy Pattern**: For flexible document processing
- **Observer Pattern**: For decoupled event handling
- **Factory Pattern**: For component creation
- **Singleton Pattern**: For resource sharing
- **Circuit Breaker Pattern**: For failure isolation
- **Dependency Injection**: For component management

For more details, see the [Architecture Documentation](AmazonQ.md).

## 📋 Requirements

- **Java**: 11
- **Apache Flink**: 1.18.1
- **MongoDB**: 5.0
- **Apache Kafka**: 2.0 or higher
- **AWS** (optional):
    - S3 for resume token storage
    - EKS for Kubernetes deployment
- **Docker** (optional): For containerized deployment
- **Kubernetes/EKS** (optional): For orchestrated deployment

## 🛠️ Building the Application

```bash
# Clone the repository
git clone https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming.git
cd MongoDb_Kafka_Data_Streaming

# Build with Maven
mvn clean package
```

This will create a JAR file with all dependencies in the `target` directory.

## ⚙️ Configuration

The application can be configured using environment variables or a properties file.

### Configuration Priority

1. Custom properties file specified as a command-line argument
2. `application.properties` file in the current directory
3. `application.properties` file in the classpath resources
4. Environment variables

### Environment Variables

```
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017
MONGODB_DATABASE=mydb
MONGODB_COLLECTION=mycollection
MONGODB_BATCH_SIZE=1000

# Resume Token Configuration (S3)
RESUME_TOKEN_BUCKET=mongo-kafka-cdc-tokens
RESUME_TOKEN_KEY=resume-token.json

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:8097,localhost:8098,localhost:8099
KAFKA_TOPIC=mongodb-data-java
KAFKA_CLIENT_ID=mongo-kafka-cdc
KAFKA_MAX_REQUEST_SIZE=1048576
KAFKA_ACKS=all

# Application Configuration
INITIAL_LOAD_ENABLED=true
INITIAL_LOAD_FORCE=false
HEALTH_PORT=8080
RETRY_MAX_ATTEMPTS=5
RETRY_BACKOFF_MS=1000
AWS_REGION=us-east-1
```

### Properties File

```properties
# MongoDB Configuration
mongodb.uri=mongodb://localhost:27017
mongodb.database=mydb
mongodb.collection=mycollection
mongodb.batch.size=1000
mongodb.resume.token.bucket=mongo-kafka-cdc-tokens
mongodb.resume.token.key=resume-token.json

# Kafka Configuration
kafka.bootstrap.servers=localhost:8097,localhost:8098,localhost:8099
kafka.topic=mongodb-data-java
kafka.client.id=mongo-kafka-cdc
kafka.max.request.size=1048576
kafka.acks=all

# Application Configuration
app.initial.load.enabled=true
app.initial.load.force=false
app.health.port=8080
app.retry.max.attempts=5
app.retry.backoff.ms=1000
```

## 🚀 Running the Application

### Local Development

```bash
# Run with application.properties from classpath resources
java -jar target/enterprise-data-ingest-0.1-jar-with-dependencies.jar

# Run with application.properties from current directory
cp src/main/resources/application.properties ./
java -jar target/enterprise-data-ingest-0.1-jar-with-dependencies.jar

# Run with custom properties file
java -jar target/enterprise-data-ingest-0.1-jar-with-dependencies.jar custom-config.properties
```

### Docker

```bash
# Build the Docker image
docker build -t enterprise-data-ingest:latest .

# Run the Docker container
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb://mongodb-host:27017 \
  -e MONGODB_DATABASE=mydb \
  -e MONGODB_COLLECTION=mycollection \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092 \
  -e KAFKA_TOPIC=mongodb-data-java \
  -e AWS_REGION=us-east-1 \
  -e AWS_ACCESS_KEY_ID=your_access_key \
  -e AWS_SECRET_ACCESS_KEY=your_secret_key \
  enterprise-data-ingest:latest
```

### Kubernetes/EKS

1. Update the Kubernetes deployment files in the `kubernetes` directory
2. Create the required secrets:
   ```bash
   kubectl apply -f kubernetes/secrets.yaml
   ```
3. Deploy the application:
   ```bash
   kubectl apply -f kubernetes/deployment.yaml
   ```

For detailed deployment instructions, see the [Deployment Guide](docs/DeploymentGuide.md).

## 🔄 How It Works

### Processing Flow

1. **Initial Load**: The application first reads all documents from the specified MongoDB collection and sends them to Kafka.
2. **Change Data Capture**: After the initial load, the application starts a MongoDB Change Stream to capture real-time changes (inserts, updates, deletes) and sends them to Kafka.
3. **Resume Token**: The application saves a resume token in S3 or local storage after processing each change event, allowing it to resume from where it left off if restarted.
4. **Horizontal Scaling**: Multiple instances can run in parallel, each processing change events from MongoDB.

### Environment Flexibility

The application is designed to run in both local development environments and AWS environments:

- **Local Mode**: When AWS credentials are not available, the application automatically uses local file storage for resume tokens.
- **AWS Mode**: When AWS credentials are available, the application uses S3 for resume token storage.

### Initial Load Configuration

The application provides two configuration options to control the initial load behavior:

- `app.initial.load.enabled` (or `INITIAL_LOAD_ENABLED` environment variable): Controls whether the initial load is enabled at all. When set to `false`, the application will skip the initial load and only process change events.

- `app.initial.load.force` (or `INITIAL_LOAD_FORCE` environment variable): When set to `true`, the application will perform the initial load even if it has been completed before. This is useful for reloading all data from MongoDB to Kafka.

Unlike previous versions, the current implementation is purely configuration-driven and does not create marker documents in MongoDB.

## 📊 Data Format

Documents are sent to Kafka as JSON strings with additional metadata:

```json
{
  "_id": "document_id",
  "field1": "value1",
  "field2": "value2",
  "nested": {
    "subfield": "subvalue"
  },
  "_operation": "insert",
  "_source": "change_stream",
  "_timestamp": "2023-04-10T12:34:56.789Z"
}
```

### Metadata Fields

- `_operation`: The operation type (read, insert, update, replace, delete)
- `_source`: The source of the event (initial_load or change_stream)
- `_timestamp`: The timestamp when the event was processed

### Kafka Record Keys

The application uses the following logic to determine the Kafka record key:

1. If the MongoDB document contains a `vuid` field, that value is used as the Kafka record key
2. Otherwise, the MongoDB document's `_id` field is used as the Kafka record key

This allows for custom routing of messages in Kafka based on your application's specific identifier.

## ⚡ Performance Optimizations

The application includes several performance optimizations:

- **Batch Processing**: Documents are processed in batches to reduce network overhead
- **Connection Pooling**: MongoDB connections are pooled for efficient reuse
- **Kafka Producer Optimizations**: Configured for optimal throughput with batching and compression
- **Java Streams**: Used for efficient document processing
- **Thread Safety**: Ensures reliable operation in concurrent environments
- **Memory Management**: Controls memory usage for processing large datasets

For more details, see the [Performance Optimizations Documentation](docs/PerformanceOptimizations.md).

## 🛡️ Resilience Features

The application includes several resilience features:

- **Circuit Breakers**: Prevent cascading failures when external services are unavailable
- **Retry Mechanisms**: Handle transient failures with configurable retry policies
- **Resume Capability**: Continue from where it left off after a restart
- **Graceful Shutdown**: Ensure proper resource cleanup and completion of in-flight operations
- **Error Handling**: Isolate and manage failures without affecting the entire application
- **Health Checks**: Enable monitoring and automated recovery
- **Metrics Collection**: Provide operational insights

For more details, see the [Resilience Features Documentation](docs/ResilienceFeatures.md).

## 📈 Monitoring

- **Health Endpoints**: The application exposes health endpoints at `/health` and `/ready` for Kubernetes probes
- **Metrics**: Collects metrics for document processing counts, processing times, and error counts
- **Logging**: Uses SLF4J with Logback for comprehensive logging
- **Circuit Breaker Status**: Monitors the state of circuit breakers for external dependencies

## ⚠️ Error Handling

- **Connection Errors**: The application will retry connecting to MongoDB and Kafka with exponential backoff
- **Processing Errors**: Individual document processing errors are logged but don't stop the application
- **Resume Capability**: If the application crashes, it will resume from the last saved resume token
- **Circuit Breakers**: Prevent cascading failures by failing fast when external services are unavailable

## 🧪 Testing

The application includes a comprehensive testing strategy:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **End-to-End Tests**: Test the complete pipeline

For more details, see the [Testing Strategy Documentation](docs/TestingStrategy.md).

## 📚 Documentation

- [Architecture Documentation](AmazonQ.md): Detailed information about the architecture and design patterns
- [Design Patterns Documentation](docs/DesignPatterns.md): Information about the design patterns used
- [Performance Optimizations Documentation](docs/PerformanceOptimizations.md): Details about performance optimizations
- [Resilience Features Documentation](docs/ResilienceFeatures.md): Information about resilience features
- [Modular Architecture Documentation](docs/ModularArchitecture.md): Details about the modular architecture
- [Testing Strategy Documentation](docs/TestingStrategy.md): Information about the testing strategy
- [Deployment Guide](docs/DeploymentGuide.md): Detailed deployment instructions
- [Project Improvements](docs/ProjectImprovements.md): Summary of key improvements made to the project

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Quick Contribution Steps
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Development Setup
```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/MongoDb_Kafka_Data_Streaming.git
cd MongoDb_Kafka_Data_Streaming

# Install dependencies and run tests
mvn clean install
mvn test
```

## ⚠️ Limitations

- The application requires MongoDB 5.0 for optimal performance
- For sharded clusters, the application needs to be configured with appropriate read preferences
- Large documents may require adjusting the Kafka `max.request.size` setting
- The application does not support MongoDB transactions spanning multiple operations
- Test compatibility issues with the new implementation structure
- Need to ensure backward compatibility with existing databases

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ⭐ Show Your Support

Give a ⭐ if this project helped you!

## 📞 Connect

- **Issues**: [GitHub Issues](https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/issues)
- **Discussions**: [GitHub Discussions](https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/discussions)
- **Author**: [@ghoshp83](https://github.com/ghoshp83)
