# Real-Time Chat Application

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

A production-grade real-time chat application built with Java Spring Boot, featuring 1:1 and group chats, presence tracking, message delivery receipts, offline message delivery, and media attachments.

## 📚 Documentation

- **[System Design](SYSTEM_DESIGN.md)**: Detailed architecture, data flow, and design decisions
- **[Deployment Guide](DEPLOYMENT_GUIDE.md)**: Production deployment, scaling, and monitoring
- **[Docker Commands](docker-commands.txt)**: Complete list of Docker terminal commands
- **[Postman Collection](postman_collection.json)**: API testing collection with all endpoints

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Load Balancer │    │   Chat App      │
│   (Web/Mobile)  │◄──►│   (Nginx/K8s)   │◄──►│   (Spring Boot) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐             │
                       │   Redis         │◄────────────┤
                       │   (Pub/Sub +    │             │
                       │   Presence)     │             │
                       └─────────────────┘             │
                                                        │
                       ┌─────────────────┐             │
                       │   PostgreSQL    │◄────────────┤
                       │   (Messages +   │             │
                       │   Users)        │             │
                       └─────────────────┘             │
                                                        │
                       ┌─────────────────┐             │
                       │   MinIO/S3      │◄────────────┤
                       │   (File Storage)│             │
                       └─────────────────┘             │
                                                        │
                       ┌─────────────────┐             │
                       │   Kafka         │◄────────────┘
                       │   (Events)      │
                       └─────────────────┘
```

## Features

### Core Features ✅
- **Authentication & Authorization**: JWT-based auth with refresh tokens
- **Real-time Messaging**: WebSocket (STOMP) with SockJS fallback
- **1:1 and Group Chats**: Direct messages and group conversations
- **Message Status**: ✔ Sent, ✔✔ Delivered, ✔✔✔ Read receipts
- **Presence Tracking**: Online/offline status with Redis heartbeat
- **Typing Indicators**: Real-time typing notifications
- **Offline Message Delivery**: Queue messages for offline users
- **Media Attachments**: Secure file upload to S3-compatible storage (MinIO)

### Advanced Features ✅
- **Multi-instance Support**: Redis pub/sub for horizontal scaling
- **Message Persistence**: PostgreSQL with optimized indexes
- **Event Streaming**: Kafka integration for analytics and notifications
- **Rate Limiting**: Bucket4j-based API protection
- **Security Audit**: Failed login tracking and account lockout
- **Enhanced File Security**: Hash validation and type checking
- **Production Monitoring**: Prometheus + Grafana integration
- **Health Monitoring**: Comprehensive Spring Actuator endpoints
- **Graceful Shutdown**: Proper connection cleanup

### Performance Optimizations ✅
- **Sub-100ms Latency**: Async processing, connection pooling, caching
- **Scalability Testing**: Load test script for thousands of concurrent users
- **Thread Pool Optimization**: Dedicated executors for messaging and WebSocket
- **Database Optimization**: Connection pooling, batch operations, indexing
- **Redis Performance**: Disabled transactions, optimized serialization
- **WebSocket Performance**: Increased cache limits, message ordering

### Deployment & Testing ✅
- **Automated Deployment**: Cross-platform deployment scripts
- **CI/CD Pipeline**: GitHub Actions with frontend/backend builds
- **Integration Testing**: Comprehensive test suite with health checks
- **Load Testing**: Scalability verification for concurrent users
- **Deployment Verification**: Automated health checks and reporting

## Tech Stack

### Backend
- **Java 17**, Spring Boot 3.2
- **Security**: Spring Security, JWT, BCrypt
- **Database**: PostgreSQL, Spring Data JPA, Flyway migrations
- **Cache/Pub-Sub**: Redis
- **Messaging**: STOMP over WebSocket, SockJS
- **File Storage**: MinIO (S3-compatible)
- **Event Streaming**: Apache Kafka (optional)

### Frontend
- **React 18** with Vite
- **TailwindCSS** for styling
- **WebSocket**: STOMP over SockJS
- **HTTP Client**: Axios with interceptors
- **State Management**: React hooks and context

### DevOps ✅
- **Containerization**: Docker, Docker Compose with production overrides
- **Orchestration**: Kubernetes with Ingress and HPA
- **CI/CD**: GitHub Actions
- **Reverse Proxy**: Nginx with SSL termination
- **Monitoring**: Prometheus, Grafana, and custom metrics
- **Clustering**: Redis Cluster and Kafka Cluster support
- **Load Balancing**: Multi-instance deployment ready

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Maven 3.6+

### Automated Deployment (Recommended)

1. **Clone the repository**
```bash
git clone <repository-url>
cd chat-app
```

2. **Deploy with automated script**
```bash
# Linux/Mac
./deploy-and-test.sh

# Windows
deploy-and-test.bat
```

3. **Access the application**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Health: http://localhost:8080/actuator/health
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)

### Manual Development Setup

1. **Start infrastructure**
```bash
docker-compose up -d postgres redis minio kafka
```

2. **Run backend**
```bash
mvn spring-boot:run
```

3. **Run frontend**
```bash
cd frontend
npm install
npm run dev
```

### Using Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Manual Development Setup

1. **Start infrastructure**
```bash
docker-compose up -d postgres redis minio kafka
```

2. **Run backend**
```bash
mvn spring-boot:run
```

3. **Run frontend**
```bash
cd frontend
npm install
npm run dev
```

### Using Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

## API Documentation

### Postman Collection

Import the provided [`postman_collection.json`](postman_collection.json) to test all API endpoints. The collection includes:

- Authentication workflows (register, login, refresh)
- Chat operations (conversations, messages, file uploads)
- Pre-configured environment variables
- Example requests with proper headers and payloads

### Authentication Endpoints

#### Register
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

#### Refresh Token
```bash
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

### Chat Endpoints

#### Get Conversations
```bash
GET /api/conversations
Authorization: Bearer <access-token>
```

#### Create Direct Conversation
```bash
POST /api/conversations/direct
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "userId": 2
}
```

#### Send Message
```bash
POST /api/messages
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "conversationId": 1,
  "content": "Hello, world!",
  "type": "TEXT"
}
```

#### Upload File
```bash
POST /api/files
Authorization: Bearer <access-token>
Content-Type: multipart/form-data

file: <binary-data>
```

## WebSocket Integration

### JavaScript Client Example

```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// Connect with JWT token
stompClient.connect({
    'Authorization': 'Bearer ' + accessToken
}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to conversation messages
    stompClient.subscribe('/topic/conversations/1', function(message) {
        const messageData = JSON.parse(message.body);
        displayMessage(messageData);
    });
    
    // Subscribe to typing indicators
    stompClient.subscribe('/topic/conversations/1/typing', function(message) {
        const typingData = JSON.parse(message.body);
        showTypingIndicator(typingData);
    });
    
    // Subscribe to offline messages
    stompClient.subscribe('/user/queue/offline-messages', function(message) {
        const messageData = JSON.parse(message.body);
        displayMessage(messageData);
    });
});

// Send message
function sendMessage(conversationId, content) {
    stompClient.send('/app/conversations/' + conversationId + '/send', {}, 
        JSON.stringify({
            'conversationId': conversationId,
            'content': content,
            'type': 'TEXT'
        })
    );
}

// Send typing indicator
function sendTypingIndicator(conversationId, typing) {
    stompClient.send('/app/conversations/' + conversationId + '/typing', {}, 
        JSON.stringify({'typing': typing})
    );
}
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | chatdb |
| `DB_USERNAME` | Database username | chatuser |
| `DB_PASSWORD` | Database password | chatpass |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `MINIO_ENDPOINT` | MinIO endpoint | http://localhost:9000 |
| `MINIO_ACCESS_KEY` | MinIO access key | minioadmin |
| `MINIO_SECRET_KEY` | MinIO secret key | minioadmin |
| `JWT_SECRET` | JWT signing secret | (required) |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers | localhost:9092 |

## Kubernetes Deployment

### Prerequisites
- Kubernetes cluster
- kubectl configured
- Docker images pushed to registry

### Deploy to Kubernetes

1. **Create namespace and apply backend manifests**
```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

2. **Deploy frontend**
```bash
kubectl apply -f frontend/k8s/frontend-deployment.yaml
kubectl apply -f frontend/k8s/ingress.yaml
```

3. **Verify deployment**
```bash
kubectl get pods -n chat-app
kubectl get services -n chat-app
kubectl get ingress -n chat-app
```

4. **Access the application**
```bash
# Port forward for testing
kubectl port-forward service/chat-frontend-service 3000:80 -n chat-app

# Or use Ingress (configure DNS)
# Frontend: http://chat.example.com
# API: http://chat.example.com/api
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

## Monitoring & Observability

### Health Checks
- **Liveness**: `/actuator/health`
- **Readiness**: `/actuator/health`
- **Metrics**: `/actuator/metrics`

### Logging
- Structured JSON logging
- Request correlation IDs
- Performance metrics

### Scaling Considerations

1. **Horizontal Scaling**: Use Redis pub/sub for multi-instance messaging
2. **Database**: Connection pooling, read replicas
3. **File Storage**: CDN for static assets
4. **Caching**: Redis for session storage and presence
5. **Load Balancing**: Sticky sessions for WebSocket connections

## Security Features

- **JWT Authentication**: Secure token-based auth
- **Password Hashing**: BCrypt with salt
- **Input Validation**: Request sanitization
- **File Upload Security**: Type and size validation
- **Rate Limiting**: Configurable throttling
- **CORS Configuration**: Cross-origin request handling

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.