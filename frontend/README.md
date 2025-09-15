# Chat App Frontend

Modern React frontend for the real-time chat application.

## Features

- **React 18** with Vite for fast development
- **TailwindCSS** for modern styling
- **WebSocket Integration** with STOMP over SockJS
- **JWT Authentication** with automatic token refresh
- **Real-time Messaging** with typing indicators
- **File Upload** support for images and documents
- **Responsive Design** for mobile and desktop

## Quick Start

### Development

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Access at http://localhost:3000
```

### Production Build

```bash
# Build for production
npm run build

# Preview production build
npm run preview
```

### Docker

```bash
# Build Docker image
docker build -t chat-frontend .

# Run container
docker run -p 3000:80 chat-frontend
```

## Environment Variables

Create `.env.local` file:

```
VITE_API_URL=http://localhost:8080/api
```

## Project Structure

```
src/
├── components/          # React components
│   ├── Auth.jsx        # Login/Register forms
│   ├── Chat.jsx        # Main chat interface
│   ├── ConversationList.jsx  # Sidebar with conversations
│   ├── MessageList.jsx # Message display
│   └── MessageInput.jsx # Message input with file upload
├── hooks/              # Custom React hooks
│   ├── useAuth.js      # Authentication management
│   └── useChat.js      # Chat functionality
├── services/           # API and WebSocket services
│   ├── api.js          # REST API client
│   └── websocket.js    # WebSocket service
└── utils/              # Utility functions
```

## Key Features

### Authentication
- JWT-based authentication with refresh tokens
- Automatic token refresh on API calls
- Persistent login state

### Real-time Messaging
- WebSocket connection with automatic reconnection
- Message delivery and read receipts
- Typing indicators
- Offline message delivery

### File Sharing
- Drag & drop file upload
- Image preview in chat
- File download links
- Upload progress indication

### UI/UX
- Responsive design for all screen sizes
- Modern chat interface with TailwindCSS
- Loading states and error handling
- Smooth animations and transitions

## Deployment

### Kubernetes

```bash
# Deploy frontend
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/ingress.yaml

# Check status
kubectl get pods -n chat-app
kubectl get services -n chat-app
```

### Docker Compose

```bash
# Start all services including frontend
docker-compose up -d

# Frontend available at http://localhost:3000
```

## API Integration

The frontend communicates with the backend through:

- **REST API** for authentication and data operations
- **WebSocket** for real-time messaging
- **File Upload** for media attachments

All API calls include JWT authentication headers and automatic token refresh.