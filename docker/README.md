# Docker Development Environment

This directory contains Docker configuration for running the complete MongoDB Kafka Data Streaming development environment.

## 🚀 Quick Start

```bash
# Navigate to docker directory
cd docker

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f mongodb-kafka-streaming

# Stop all services
docker-compose down
```

## 📋 Services Included

| Service | Port | Description |
|---------|------|-------------|
| **MongoDB** | 27017 | MongoDB with replica set for CDC |
| **Kafka** | 9092 | Apache Kafka message broker |
| **Zookeeper** | 2181 | Kafka coordination service |
| **Kafka UI** | 8080 | Web interface for Kafka monitoring |
| **MongoDB Express** | 8081 | Web interface for MongoDB |
| **Application** | 8082 | MongoDB Kafka Streaming app |

## 🔧 Configuration

The application is pre-configured with:
- MongoDB: `mongodb://mongodb:27017/mydb`
- Kafka: `kafka:29092`
- Sample data automatically created
- Health checks enabled

## 📊 Monitoring

- **Kafka UI**: http://localhost:8080
- **MongoDB Express**: http://localhost:8081 (admin/password)
- **Application Health**: http://localhost:8082/health

## 🗂️ Files

- `docker-compose.yml`: Complete development environment
- `init-mongo.js`: MongoDB initialization script
- `README.md`: This documentation