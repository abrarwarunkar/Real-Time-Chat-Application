import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.subscriptions = new Map();
  }

  connect(token) {
    return new Promise((resolve, reject) => {
      const socket = new SockJS('/ws');
      this.stompClient = Stomp.over(socket);
      
      this.stompClient.connect(
        { Authorization: `Bearer ${token}` },
        (frame) => {
          this.connected = true;
          console.log('WebSocket connected:', frame);
          
          // Subscribe to offline messages
          this.subscribe('/user/queue/offline-messages', (message) => {
            const data = JSON.parse(message.body);
            this.onMessage?.(data);
          });

          // Subscribe to message status updates
          this.subscribe('/user/queue/message-status', (message) => {
            const data = JSON.parse(message.body);
            this.onMessageStatus?.(data);
          });

          resolve();
        },
        (error) => {
          this.connected = false;
          console.error('WebSocket connection error:', error);
          reject(error);
        }
      );
    });
  }

  disconnect() {
    if (this.stompClient) {
      this.subscriptions.clear();
      this.stompClient.disconnect();
      this.connected = false;
    }
  }

  subscribe(destination, callback) {
    if (this.stompClient && this.connected) {
      const subscription = this.stompClient.subscribe(destination, callback);
      this.subscriptions.set(destination, subscription);
      return subscription;
    }
  }

  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
    }
  }

  subscribeToConversation(conversationId, callbacks = {}) {
    const destinations = {
      messages: `/topic/conversations/${conversationId}`,
      typing: `/topic/conversations/${conversationId}/typing`,
      status: `/topic/conversations/${conversationId}/status`,
    };

    Object.entries(destinations).forEach(([type, destination]) => {
      this.subscribe(destination, (message) => {
        const data = JSON.parse(message.body);
        callbacks[type]?.(data);
      });
    });
  }

  unsubscribeFromConversation(conversationId) {
    const destinations = [
      `/topic/conversations/${conversationId}`,
      `/topic/conversations/${conversationId}/typing`,
      `/topic/conversations/${conversationId}/status`,
    ];
    
    destinations.forEach(destination => this.unsubscribe(destination));
  }

  sendMessage(conversationId, message) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(
        `/app/conversations/${conversationId}/send`,
        {},
        JSON.stringify(message)
      );
    }
  }

  sendTypingIndicator(conversationId, typing) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(
        `/app/conversations/${conversationId}/typing`,
        {},
        JSON.stringify({ typing })
      );
    }
  }

  // Event handlers (set by components)
  onMessage = null;
  onMessageStatus = null;
  onTyping = null;
}

export default new WebSocketService();