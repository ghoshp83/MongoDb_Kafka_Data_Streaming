# MongoDB to Kafka CDC - Deployment Guide

This guide provides detailed instructions for deploying the MongoDB to Kafka CDC application in various environments, from local development to production on AWS EKS.

## Prerequisites

Before deploying the application, ensure you have the following prerequisites:

- Java 11 or higher
- MongoDB 4.0 or higher (with Change Streams enabled)
- Apache Kafka 2.0 or higher
- AWS account (for S3 and EKS deployment)
- Docker (for containerized deployment)
- Kubernetes CLI (kubectl) for Kubernetes deployment

## Local Deployment

### Building the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/mongodb-kafka-cdc.git
   cd mongodb-kafka-cdc
   ```

2. Build the application:
   ```bash
   mvn clean package
   ```

3. Verify the build:
   ```bash
   ls -la target/mongodb-kafka-cdc-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

### Configuration

Create an `application.properties` file with your configuration:

```properties
# MongoDB Configuration
mongodb.uri=mongodb://localhost:27017
mongodb.database=mydb
mongodb.collection=mycollection
mongodb.batch.size=1000

# Kafka Configuration
kafka.bootstrap.servers=localhost:9092
kafka.topic=mongodb-data
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

### Running the Application

Run the application with the configuration file:

```bash
java -jar target/mongodb-kafka-cdc-1.0-SNAPSHOT-jar-with-dependencies.jar application.properties
```

Or use environment variables:

```bash
export MONGODB_URI=mongodb://localhost:27017
export MONGODB_DATABASE=mydb
export MONGODB_COLLECTION=mycollection
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_TOPIC=mongodb-data
java -jar target/mongodb-kafka-cdc-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Docker Deployment

### Creating a Dockerfile

Create a `Dockerfile` in the project root:

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/mongodb-kafka-cdc-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Building the Docker Image

Build the Docker image:

```bash
docker build -t mongo-kafka-cdc:latest .
```

### Running the Docker Container

Run the Docker container with environment variables:

```bash
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb://mongodb-host:27017 \
  -e MONGODB_DATABASE=mydb \
  -e MONGODB_COLLECTION=mycollection \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092 \
  -e KAFKA_TOPIC=mongodb-data \
  mongo-kafka-cdc:latest
```

## Kubernetes Deployment

### Creating Kubernetes Manifests

#### ConfigMap

Create a ConfigMap for non-sensitive configuration:

```yaml
# kubernetes/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mongo-kafka-cdc-config
data:
  MONGODB_DATABASE: "mydb"
  MONGODB_COLLECTION: "mycollection"
  MONGODB_BATCH_SIZE: "1000"
  KAFKA_TOPIC: "mongodb-data"
  KAFKA_CLIENT_ID: "mongo-kafka-cdc"
  KAFKA_MAX_REQUEST_SIZE: "1048576"
  KAFKA_ACKS: "all"
  INITIAL_LOAD_ENABLED: "true"
  INITIAL_LOAD_FORCE: "false"
  HEALTH_PORT: "8080"
  RETRY_MAX_ATTEMPTS: "5"
  RETRY_BACKOFF_MS: "1000"
```

#### Secrets

Create a Secret for sensitive configuration:

```yaml
# kubernetes/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: mongo-kafka-cdc-secrets
type: Opaque
data:
  MONGODB_URI: bW9uZ29kYjovL3VzZXJuYW1lOnBhc3N3b3JkQG1vbmdvZGItaG9zdDoyNzAxNw==
  KAFKA_BOOTSTRAP_SERVERS: a2Fma2EtaG9zdDo5MDky
  AWS_ACCESS_KEY_ID: eW91ci1hY2Nlc3Mta2V5
  AWS_SECRET_ACCESS_KEY: eW91ci1zZWNyZXQta2V5
  AWS_REGION: dXMtZWFzdC0x
```

#### Deployment

Create a Deployment:

```yaml
# kubernetes/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongo-kafka-cdc
  labels:
    app: mongo-kafka-cdc
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongo-kafka-cdc
  template:
    metadata:
      labels:
        app: mongo-kafka-cdc
    spec:
      containers:
      - name: mongo-kafka-cdc
        image: mongo-kafka-cdc:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: mongo-kafka-cdc-config
        - secretRef:
            name: mongo-kafka-cdc-secrets
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

#### Service

Create a Service for the health check endpoints:

```yaml
# kubernetes/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: mongo-kafka-cdc
spec:
  selector:
    app: mongo-kafka-cdc
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP
```

### Deploying to Kubernetes

Apply the Kubernetes manifests:

```bash
kubectl apply -f kubernetes/configmap.yaml
kubectl apply -f kubernetes/secrets.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
```

## AWS EKS Deployment

### Prerequisites

- AWS CLI configured with appropriate permissions
- eksctl installed
- kubectl installed

### Creating an EKS Cluster

Create an EKS cluster:

```bash
eksctl create cluster \
  --name mongo-kafka-cdc-cluster \
  --version 1.21 \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 3 \
  --managed
```

### Pushing the Docker Image to ECR

1. Create an ECR repository:
   ```bash
   aws ecr create-repository --repository-name mongo-kafka-cdc
   ```

2. Log in to ECR:
   ```bash
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <your-account-id>.dkr.ecr.us-east-1.amazonaws.com
   ```

3. Tag and push the image:
   ```bash
   docker tag mongo-kafka-cdc:latest <your-account-id>.dkr.ecr.us-east-1.amazonaws.com/mongo-kafka-cdc:latest
   docker push <your-account-id>.dkr.ecr.us-east-1.amazonaws.com/mongo-kafka-cdc:latest
   ```

### Creating IAM Role for S3 Access

Create an IAM role for S3 access:

```bash
cat > trust-policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "eks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

aws iam create-role --role-name mongo-kafka-cdc-s3-role --assume-role-policy-document file://trust-policy.json

aws iam attach-role-policy --role-name mongo-kafka-cdc-s3-role --policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess
```

### Updating Kubernetes Manifests for EKS

Update the deployment manifest to use the ECR image:

```yaml
# kubernetes/eks-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongo-kafka-cdc
  labels:
    app: mongo-kafka-cdc
spec:
  replicas: 2  # Increased for high availability
  selector:
    matchLabels:
      app: mongo-kafka-cdc
  template:
    metadata:
      labels:
        app: mongo-kafka-cdc
    spec:
      serviceAccountName: mongo-kafka-cdc-sa
      containers:
      - name: mongo-kafka-cdc
        image: <your-account-id>.dkr.ecr.us-east-1.amazonaws.com/mongo-kafka-cdc:latest
        # Rest of the configuration remains the same
```

Create a ServiceAccount with IAM role:

```yaml
# kubernetes/service-account.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: mongo-kafka-cdc-sa
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::<your-account-id>:role/mongo-kafka-cdc-s3-role
```

### Deploying to EKS

Apply the Kubernetes manifests:

```bash
kubectl apply -f kubernetes/service-account.yaml
kubectl apply -f kubernetes/configmap.yaml
kubectl apply -f kubernetes/secrets.yaml
kubectl apply -f kubernetes/eks-deployment.yaml
kubectl apply -f kubernetes/service.yaml
```

## Horizontal Scaling

The application is designed to support horizontal scaling. When running multiple instances:

1. Each instance will connect to MongoDB and listen for change events
2. MongoDB will distribute change events among the instances
3. Each instance will process its share of events and send them to Kafka

To scale the application:

```bash
kubectl scale deployment mongo-kafka-cdc --replicas=3
```

## Monitoring

### Health Checks

The application exposes health check endpoints:

- `/health`: Returns 200 OK if the application is healthy
- `/ready`: Returns 200 OK if the application is ready to receive requests

### Logging

The application uses SLF4J with Logback for logging. Configure Logback by creating a `logback.xml` file in the classpath:

```xml
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  
  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/application.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
      <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  
  <root level="INFO">
    <appender-ref ref="CONSOLE" />
    <appender-ref ref="FILE" />
  </root>
  
  <!-- Set specific loggers to DEBUG for more detailed logging -->
  <logger name="com.enterprise.department" level="INFO" />
</configuration>
```

### Metrics

The application collects metrics that can be exposed to monitoring systems:

- Document processing counts
- Processing times
- Error counts
- Circuit breaker states

## Troubleshooting

### Common Issues

1. **MongoDB Connection Issues**
   - Check MongoDB URI and credentials
   - Verify network connectivity
   - Ensure MongoDB version supports Change Streams (4.0+)

2. **Kafka Connection Issues**
   - Check Kafka bootstrap servers
   - Verify network connectivity
   - Check topic exists and is writable

3. **S3 Access Issues**
   - Verify AWS credentials
   - Check IAM permissions
   - Ensure S3 bucket exists and is accessible

### Checking Logs

View application logs:

```bash
# For local deployment
tail -f logs/application.log

# For Kubernetes deployment
kubectl logs -f deployment/mongo-kafka-cdc

# For specific pod
kubectl logs -f <pod-name>
```

### Checking Application Status

Check the application status:

```bash
# For local deployment
curl http://localhost:8080/health

# For Kubernetes deployment
kubectl port-forward service/mongo-kafka-cdc 8080:8080
curl http://localhost:8080/health
```

## Conclusion

This deployment guide provides comprehensive instructions for deploying the MongoDB to Kafka CDC application in various environments. By following these instructions, you can deploy the application locally for development, in Docker containers for testing, or in Kubernetes/EKS for production use.

The application's design allows for flexible deployment options and horizontal scaling to meet your specific requirements.
