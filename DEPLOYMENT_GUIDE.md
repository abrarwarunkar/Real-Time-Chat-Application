# Production Deployment Guide

## Overview

This guide covers deploying the Real-Time Chat Application to production with Redis Pub/Sub and Kafka integration for high scalability and reliability.

## Architecture Components

### Core Services
- **Chat Application**: Spring Boot with WebSocket support
- **PostgreSQL**: Primary database for persistent data
- **Redis**: Caching, session storage, and pub/sub messaging
- **Kafka**: Event streaming for analytics and notifications
- **MinIO**: S3-compatible file storage

### New Production Features
- **Redis Pub/Sub**: Multi-instance message broadcasting
- **Kafka Events**: Analytics and event-driven architecture
- **Enhanced Presence**: Real-time online/offline tracking
- **Heartbeat System**: Connection health monitoring

## Local Development Setup

### 1. Start All Services
```bash
# Start infrastructure services
docker-compose up -d

# Verify services are running
docker-compose ps

# Check logs
docker-compose logs -f app
```

### 2. Access Points
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **Kafka UI**: http://localhost:8081

### 3. Test Redis Pub/Sub
```bash
# Connect to Redis CLI
docker exec -it $(docker-compose ps -q redis) redis-cli

# Subscribe to message channel
SUBSCRIBE chat.messages

# In another terminal, publish a test message
PUBLISH chat.messages '{"conversationId":1,"messageData":{"content":"test"}}'
```

### 4. Test Kafka Events
```bash
# List Kafka topics
docker exec -it $(docker-compose ps -q kafka) kafka-topics --bootstrap-server localhost:9092 --list

# Consume message events
docker exec -it $(docker-compose ps -q kafka) kafka-console-consumer --bootstrap-server localhost:9092 --topic chat.message.events --from-beginning
```

## Production Deployment

### 1. Environment Variables

Create production environment file:

```bash
# Database
DB_HOST=your-postgres-host
DB_PORT=5432
DB_NAME=chatdb_prod
DB_USERNAME=chatuser_prod
DB_PASSWORD=secure_password

# Redis
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=redis_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092
KAFKA_EVENTS_ENABLED=true

# MinIO/S3
MINIO_ENDPOINT=https://your-s3-endpoint
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET=chat-files-prod

# Security
JWT_SECRET=your-super-secure-jwt-secret-key-here

# Application
SPRING_PROFILES_ACTIVE=prod
```

### 2. Kubernetes Deployment

Update the existing Kubernetes manifests:

```yaml
# k8s/deployment.yaml - Add Redis and Kafka environment variables
env:
- name: REDIS_HOST
  value: "redis-service"
- name: KAFKA_BOOTSTRAP_SERVERS
  value: "kafka-service:9092"
- name: KAFKA_EVENTS_ENABLED
  value: "true"
```

Deploy to Kubernetes:
```bash
kubectl apply -f k8s/
kubectl get pods -n chat-app
kubectl logs -f deployment/chat-app -n chat-app
```

### 3. Scaling Configuration

#### Horizontal Pod Autoscaler
```yaml
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: chat-app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: chat-app
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

#### Redis Cluster (Production)
```yaml
# redis-cluster.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis-cluster
spec:
  serviceName: redis-cluster
  replicas: 6
  selector:
    matchLabels:
      app: redis-cluster
  template:
    metadata:
      labels:
        app: redis-cluster
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        - containerPort: 16379
        command:
        - redis-server
        - /etc/redis/redis.conf
        - --cluster-enabled
        - "yes"
```

## Monitoring and Observability

### 1. Application Metrics

The application exposes metrics at `/actuator/metrics` and `/actuator/prometheus`.

Key metrics to monitor:
- **WebSocket Connections**: `websocket.connections.active`
- **Message Throughput**: `kafka.producer.record-send-rate`
- **Redis Operations**: `redis.operations.total`
- **Database Connections**: `hikaricp.connections.active`

### 2. Health Checks

Configure health check endpoints:
```yaml
# Kubernetes liveness probe
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30

# Kubernetes readiness probe
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

### 3. Logging Configuration

Production logging configuration:
```yaml
# application-prod.yml
logging:
  level:
    com.example.chat: INFO
    org.springframework.kafka: WARN
    redis.clients: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"
  appender:
    console:
      encoder:
        pattern: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"
```

## Performance Tuning

### 1. JVM Configuration
```bash
# Dockerfile - Add JVM tuning
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"
```

### 2. Database Optimization
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 25
        order_inserts: true
        order_updates: true
```

### 3. Redis Configuration
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 2
        shutdown-timeout: 100ms
```

## Security Considerations

### 1. Network Security
- Use TLS for all external communications
- Configure Redis AUTH and SSL
- Enable Kafka SASL/SSL authentication
- Use private networks for internal communication

### 2. Application Security
- Rotate JWT secrets regularly
- Implement rate limiting per user
- Validate all file uploads
- Use HTTPS-only cookies

### 3. Infrastructure Security
- Regular security updates
- Network segmentation
- Access control and monitoring
- Backup encryption

## Troubleshooting

### Common Issues

1. **WebSocket Connection Failures**
   ```bash
   # Check load balancer sticky sessions
   kubectl describe ingress chat-app-ingress
   ```

2. **Redis Pub/Sub Not Working**
   ```bash
   # Verify Redis connectivity
   kubectl exec -it redis-pod -- redis-cli ping
   ```

3. **Kafka Consumer Lag**
   ```bash
   # Check consumer group status
   kafka-consumer-groups --bootstrap-server kafka:9092 --describe --group chat-analytics
   ```

4. **High Memory Usage**
   ```bash
   # Monitor JVM heap
   kubectl exec -it chat-app-pod -- jcmd 1 GC.run_finalization
   ```

### Performance Monitoring

Monitor these key metrics:
- Message delivery latency
- WebSocket connection count
- Redis memory usage
- Kafka consumer lag
- Database connection pool utilization

## Backup and Recovery

### 1. Database Backup
```bash
# Automated PostgreSQL backup
kubectl create cronjob postgres-backup --image=postgres:15 --schedule="0 2 * * *" -- pg_dump -h postgres-service -U chatuser chatdb > /backup/chatdb-$(date +%Y%m%d).sql
```

### 2. Redis Backup
```bash
# Redis RDB backup
kubectl exec redis-pod -- redis-cli BGSAVE
```

### 3. File Storage Backup
```bash
# MinIO backup using mc client
mc mirror minio/chat-files s3/backup-bucket/chat-files
```

This deployment guide provides a comprehensive approach to running the enhanced chat application in production with Redis Pub/Sub and Kafka integration.