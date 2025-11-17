# 🔧 Troubleshooting Guide

## Common Issues and Solutions

### 1. Connection Issues

#### MongoDB Connection Failed
```
ERROR: MongoDB connection failed: Connection refused
```

**Solutions:**
```bash
# Check MongoDB status
docker ps | grep mongo
mongosh --eval "db.runCommand({ping: 1})"

# Verify connection string
export MONGODB_URI="mongodb://localhost:27017"

# Check network connectivity
telnet localhost 27017
```

#### Kafka Connection Failed
```
ERROR: Kafka connection failed: Connection refused
```

**Solutions:**
```bash
# Check Kafka status
docker ps | grep kafka
kafka-broker-api-versions --bootstrap-server localhost:9092

# Verify Kafka configuration
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"

# Check Kafka logs
docker logs kafka-container
```

### 2. Performance Issues

#### Low Throughput
**Symptoms:** Processing < 1000 docs/sec

**Solutions:**
```properties
# Increase batch sizes
mongodb.batch.size=2000
kafka.batch.size=16384

# Optimize Kafka producer
kafka.linger.ms=5
kafka.compression.type=snappy

# Increase connection pool
mongodb.connection.pool.size=20
```

#### High Memory Usage
**Symptoms:** Memory usage > 1GB

**Solutions:**
```bash
# Reduce batch size
mongodb.batch.size=500

# Tune JVM settings
JAVA_OPTS="-Xmx1g -XX:+UseG1GC"

# Monitor memory usage
jstat -gc <pid> 5s
```

### 3. Data Issues

#### Missing Documents
**Symptoms:** Documents not appearing in Kafka

**Debugging Steps:**
```bash
# Check application logs
tail -f logs/mongodb-kafka-cdc.log

# Verify MongoDB query
mongosh --eval "db.collection.find().limit(5)"

# Check Kafka topic
kafka-console-consumer --bootstrap-server localhost:9092 --topic mongodb-data --from-beginning
```

#### Duplicate Documents
**Symptoms:** Same document appears multiple times

**Solutions:**
```properties
# Enable idempotent producer
kafka.enable.idempotence=true

# Check resume token handling
app.initial.load.force=false

# Verify unique keys
kafka.key.serializer=org.apache.kafka.common.serialization.StringSerializer
```

### 4. Configuration Issues

#### Invalid Configuration
```
ERROR: Invalid configuration parameter
```

**Solutions:**
```bash
# Validate configuration
java -jar app.jar --validate-config

# Check environment variables
env | grep -E "(MONGODB|KAFKA)"

# Use configuration template
cp application.properties.template application.properties
```

#### AWS S3 Access Denied
```
ERROR: Access denied to S3 bucket
```

**Solutions:**
```bash
# Check AWS credentials
aws sts get-caller-identity

# Verify S3 permissions
aws s3 ls s3://your-bucket/

# Set correct region
export AWS_REGION=us-east-1
```

### 5. Docker Issues

#### Container Won't Start
```
ERROR: Container exited with code 1
```

**Solutions:**
```bash
# Check container logs
docker logs mongodb-kafka-streaming

# Verify environment variables
docker run --env-file .env your-image

# Check resource limits
docker stats
```

#### Port Already in Use
```
ERROR: Port 8080 already in use
```

**Solutions:**
```bash
# Find process using port
lsof -i :8080
netstat -tulpn | grep 8080

# Kill process or use different port
export HEALTH_PORT=8081
```

### 6. Kubernetes Issues

#### Pod CrashLoopBackOff
```
kubectl get pods
NAME                     READY   STATUS             RESTARTS
app-xxx                  0/1     CrashLoopBackOff   5
```

**Solutions:**
```bash
# Check pod logs
kubectl logs app-xxx

# Describe pod for events
kubectl describe pod app-xxx

# Check resource limits
kubectl top pod app-xxx

# Verify ConfigMap/Secrets
kubectl get configmap
kubectl get secrets
```

#### Health Check Failing
```
Readiness probe failed: HTTP probe failed
```

**Solutions:**
```bash
# Test health endpoint locally
curl http://localhost:8080/health

# Check service configuration
kubectl get svc
kubectl describe svc your-service

# Verify health check configuration
kubectl describe pod app-xxx | grep -A 5 "Readiness"
```

## Debugging Tools

### 1. Application Logs
```bash
# Enable debug logging
export LOG_LEVEL=DEBUG

# Tail application logs
tail -f logs/mongodb-kafka-cdc.log

# Search for specific errors
grep -i "error\|exception" logs/mongodb-kafka-cdc.log
```

### 2. Health Checks
```bash
# Application health
curl http://localhost:8080/health

# MongoDB health
mongosh --eval "db.runCommand({ping: 1})"

# Kafka health
kafka-broker-api-versions --bootstrap-server localhost:9092
```

### 3. Monitoring Commands
```bash
# JVM monitoring
jstat -gc <pid> 5s
jmap -histo <pid>

# System monitoring
top -p <pid>
iostat -x 1

# Network monitoring
netstat -i
ss -tuln
```

### 4. Performance Profiling
```bash
# Enable JFR profiling
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=profile.jfr -jar app.jar

# Memory dump
jmap -dump:format=b,file=heap.hprof <pid>

# Thread dump
jstack <pid> > threads.txt
```

## Getting Help

### 1. Log Collection
Before reporting issues, collect these logs:
```bash
# Application logs
cp logs/mongodb-kafka-cdc.log issue-logs/

# System information
uname -a > issue-logs/system-info.txt
java -version > issue-logs/java-version.txt

# Configuration (remove sensitive data)
env | grep -E "(MONGODB|KAFKA)" > issue-logs/config.txt
```

### 2. Issue Reporting
Include this information when reporting issues:
- Application version
- Java version
- MongoDB version
- Kafka version
- Operating system
- Configuration (sanitized)
- Error logs
- Steps to reproduce

### 3. Community Support
- **GitHub Issues**: [Report bugs](https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/issues)
- **GitHub Discussions**: [Ask questions](https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/discussions)
- **Documentation**: [Read docs](https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/tree/main/docs)

## Prevention Tips

### 1. Monitoring Setup
```properties
# Enable metrics collection
app.metrics.enabled=true
app.health.checks.enabled=true

# Set up alerting thresholds
app.alert.memory.threshold=85
app.alert.latency.threshold=100
```

### 2. Regular Maintenance
```bash
# Weekly log rotation
logrotate /etc/logrotate.d/mongodb-kafka

# Monthly performance review
./scripts/performance-check.sh

# Quarterly dependency updates
mvn versions:display-dependency-updates
```

### 3. Best Practices
- Monitor resource usage regularly
- Keep dependencies updated
- Use appropriate batch sizes
- Implement proper error handling
- Set up comprehensive logging
- Test configuration changes in staging first