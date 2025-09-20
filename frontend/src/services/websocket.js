import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const API_BASE = import.meta.env.VITE_API_URL ? import.meta.env.VITE_API_URL + '/api' : '/api';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.subscriptions = new Map();
  }

  connect(token) {
    return new Promise((resolve, reject) => {
      const socket = new SockJS(API_BASE + '/ws');
      this.stompClient = Stomp.over(socket);

      // Disable debug logs in production
      this.stompClient.debug = null;

      this.stompClient.connect(
        { Authorization: `Bearer ${token}` },
        (frame) => {
          console.log('WebSocket connected:', frame);

          // Wait a bit for the connection to be fully established
          setTimeout(() => {
            this.connected = true;

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
          }, 100);
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
    if (this.stompClient && this.connected && this.stompClient.connected) {
      try {
        const subscription = this.stompClient.subscribe(destination, callback);
        this.subscriptions.set(destination, subscription);
        return subscription;
      } catch (error) {
        console.error('Failed to subscribe to', destination, error);
        return null;
      }
    } else {
      console.warn('Cannot subscribe - WebSocket not connected');
      return null;
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
    if (!this.connected || !this.stompClient?.connected) {
      console.warn('Cannot subscribe to conversation - WebSocket not connected');
      return;
    }

    const destinations = {
      messages: `/topic/conversations/${conversationId}`,
      typing: `/topic/conversations/${conversationId}/typing`,
      status: `/topic/conversations/${conversationId}/status`,
    };

    Object.entries(destinations).forEach(([type, destination]) => {
      const subscription = this.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body);
          callbacks[type]?.(data);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      });

      if (!subscription) {
        console.warn(`Failed to subscribe to ${type} for conversation ${conversationId}`);
      }
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
    if (this.stompClient && this.connected && this.stompClient.connected) {
      try {
        this.stompClient.send(
          `/app/conversations/${conversationId}/send`,
          {},
          JSON.stringify(message)
        );
      } catch (error) {
        console.error('Failed to send message:', error);
      }
    } else {
      console.warn('Cannot send message - WebSocket not connected');
    }
  }

  sendTypingIndicator(conversationId, typing) {
    if (this.stompClient && this.connected && this.stompClient.connected) {
      try {
        this.stompClient.send(
          `/app/conversations/${conversationId}/typing`,
          {},
          JSON.stringify({ typing })
        );
      } catch (error) {
        console.error('Failed to send typing indicator:', error);
      }
    } else {
      console.warn('Cannot send typing indicator - WebSocket not connected');
    }
  }

  // Event handlers (set by components)
  onMessage = null;
  onMessageStatus = null;
  onTyping = null;
}

export default new WebSocketService();