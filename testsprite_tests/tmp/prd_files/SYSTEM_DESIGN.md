# Real-Time Chat Application - System Design

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Load Balancer │    │   Chat App      │
│   (React/Vite)  │◄──►│   (Nginx/K8s)   │◄──►│   (Spring Boot) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐             │
                       │   Redis         │◄────────────┤
                       │   (Cache +      │             │
                       │   Pub/Sub)      │             │
                       └─────────────────┘             │
                                                        │
                       ┌─────────────────┐             │
                       │   PostgreSQL    │◄────────────┤
                       │   (Primary DB)  │             │
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

## Core Components

### 1. Frontend Layer
- **Technology**: React 18 + Vite + TailwindCSS
- **Communication**: REST API + WebSocket (STOMP)
- **State Management**: React Context + Hooks
- **Authentication**: JWT tokens with refresh mechanism

### 2. Backend Layer
- **Framework**: Spring Boot 3.2 + Java 17
- **Security**: Spring Security + JWT
- **WebSocket**: STOMP over SockJS
- **API**: RESTful endpoints

### 3. Data Layer
- **Primary Database**: PostgreSQL
- **Cache**: Redis (sessions, presence, pub/sub)
- **File Storage**: MinIO (S3-compatible)
- **Message Queue**: Apache Kafka (optional)

## Data Flow

### Message Flow
```
User A ──► Frontend ──► WebSocket ──► Spring Boot ──► Redis Pub/Sub ──► User B
                                           │
                                           ▼
                                    PostgreSQL
                                    (Persistence)
```

### Authentication Flow
```
Login ──► JWT Token ──► Redis Cache ──► Refresh Token ──► New JWT
```

## Database Schema

### Core Tables
```sql
users (id, username, email, password_hash, created_at, last_seen)
conversations (id, name, type, created_at, updated_at)
conversation_members (conversation_id, user_id, joined_at, role)
messages (id, conversation_id, sender_id, content, type, created_at, status)
refresh_tokens (id, user_id, token, expires_at, created_at)
```

## Scalability Design

### Horizontal Scaling
- **Stateless Services**: Spring Boot instances
- **Load Balancing**: Nginx/K8s Ingress
- **Session Storage**: Redis cluster
- **Database**: PostgreSQL read replicas

### Real-time Communication
- **WebSocket Scaling**: Redis pub/sub for multi-instance
- **Message Delivery**: Guaranteed delivery with acknowledgments
- **Presence Tracking**: Redis-based online/offline status

## Security Architecture

### Authentication & Authorization
- **JWT Access Tokens**: Short-lived (15 min)
- **Refresh Tokens**: Long-lived (24 hours)
- **Password Security**: BCrypt hashing
- **API Security**: Rate limiting + CORS

### Data Protection
- **Input Validation**: Spring Validation
- **SQL Injection**: JPA/Hibernate protection
- **File Upload**: Type/size validation
- **HTTPS**: TLS encryption

## Performance Optimizations

### Caching Strategy
- **User Sessions**: Redis cache
- **Conversation Lists**: Redis with TTL
- **Message Pagination**: Database indexing
- **File Serving**: CDN integration

### Database Optimization
- **Indexes**: On frequently queried columns
- **Connection Pooling**: HikariCP
- **Query Optimization**: JPA query hints
- **Read Replicas**: For scaling reads

## Monitoring & Observability

### Health Checks
- **Application**: Spring Actuator endpoints
- **Database**: Connection health
- **Redis**: Ping/pong checks
- **External Services**: MinIO/Kafka health

### Metrics & Logging
- **Application Metrics**: Micrometer + Prometheus
- **Structured Logging**: JSON format with correlation IDs
- **Performance Monitoring**: Response times, throughput
- **Error Tracking**: Exception monitoring

## Deployment Architecture

### Containerization
```
Docker Images:
├── chat-app:latest (Spring Boot)
├── chat-frontend:latest (React/Nginx)
└── Infrastructure (PostgreSQL, Redis, MinIO)
```

### Kubernetes Deployment
```
Namespace: chat-app
├── Deployments (app, frontend)
├── Services (internal communication)
├── Ingress (external access)
├── ConfigMaps (configuration)
├── Secrets (credentials)
└── HPA (auto-scaling)
```

## Message Delivery Guarantees

### Delivery Semantics
- **At-least-once**: Message persistence + acknowledgments
- **Ordering**: Per-conversation message ordering
- **Offline Support**: Message queuing for offline users
- **Retry Logic**: Exponential backoff for failed deliveries

### Status Tracking
```
Message States:
SENT ──► DELIVERED ──► READ
  │         │          │
  └─────────┴──────────┴──► FAILED (with retry)
```

## File Handling Architecture

### Upload Flow
```
Client ──► Multipart Upload ──► Validation ──► MinIO ──► URL Generation ──► Database
```

### Security Measures
- **File Type Validation**: MIME type checking
- **Size Limits**: Configurable per file type
- **Virus Scanning**: Optional integration
- **Access Control**: Signed URLs with expiration

## Event-Driven Architecture (Optional)

### Kafka Integration
```
Events:
├── user.registered
├── message.sent
├── conversation.created
├── user.online/offline
└── file.uploaded
```

### Event Consumers
- **Analytics Service**: User behavior tracking
- **Notification Service**: Push notifications
- **Audit Service**: Security and compliance logging