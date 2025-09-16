import { useState, useEffect, useCallback } from 'react';
import { conversationAPI, messageAPI } from '../services/api';
import websocketService from '../services/websocket';

export const useChat = () => {
  const [conversations, setConversations] = useState([]);
  const [currentConversation, setCurrentConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [typingUsers, setTypingUsers] = useState(new Set());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadConversations();
    
    // Connect WebSocket immediately
    const token = localStorage.getItem('accessToken');
    if (token && !websocketService.connected) {
      websocketService.connect(token).catch(console.error);
    }
    
    // Set up WebSocket event handlers
    websocketService.onMessage = handleNewMessage;
    websocketService.onMessageStatus = handleMessageStatus;
    
    return () => {
      websocketService.onMessage = null;
      websocketService.onMessageStatus = null;
    };
  }, []);

  const loadConversations = async () => {
    try {
      setLoading(true);
      const response = await conversationAPI.getConversations();
      setConversations(response.data);
    } catch (error) {
      console.error('Failed to load conversations:', error);
    } finally {
      setLoading(false);
    }
  };

  const selectConversation = async (conversation) => {
    if (currentConversation?.id) {
      websocketService.unsubscribeFromConversation(currentConversation.id);
    }

    setCurrentConversation(conversation);
    setMessages([]);
    setTypingUsers(new Set());

    // Clear unread count immediately when conversation is selected
    setConversations(prev => 
      prev.map(conv => 
        conv.id === conversation.id 
          ? { ...conv, unreadCount: 0 }
          : conv
      )
    );

    try {
      // Connect WebSocket if not connected
      if (!websocketService.connected) {
        const token = localStorage.getItem('accessToken');
        if (token) {
          await websocketService.connect(token);
        }
      }
      
      const response = await conversationAPI.getMessages(conversation.id);
      setMessages(response.data.content.reverse());
      
      // Mark conversation as read
      if (response.data.content.length > 0) {
        const lastMessage = response.data.content[response.data.content.length - 1];
        markAsRead(lastMessage.id);
      }
      
      // Subscribe to conversation events
      websocketService.subscribeToConversation(conversation.id, {
        messages: handleNewMessage,
        typing: handleTypingIndicator,
        status: handleMessageStatus,
      });
    } catch (error) {
      console.error('Failed to load messages:', error);
    }
  };

  const sendMessage = useCallback((content, type = 'TEXT', attachmentUrl = null, mimeType = null) => {
    if (!currentConversation) return;

    const messageData = {
      conversationId: currentConversation.id,
      content,
      type,
      attachmentUrl,
      mimeType,
    };

    // Send message via WebSocket - backend will save and broadcast
    websocketService.sendMessage(currentConversation.id, messageData);
  }, [currentConversation]);

  const sendTypingIndicator = useCallback((typing) => {
    if (currentConversation) {
      websocketService.sendTypingIndicator(currentConversation.id, typing);
    }
  }, [currentConversation]);

  const createDirectConversation = async (userId) => {
    try {
      const response = await conversationAPI.createDirect(userId);
      const newConversation = response.data;
      setConversations(prev => [newConversation, ...prev]);
      return newConversation;
    } catch (error) {
      console.error('Failed to create conversation:', error);
      throw error;
    }
  };

  const markAsRead = async (messageId) => {
    if (currentConversation) {
      try {
        await conversationAPI.markAsRead(currentConversation.id, messageId);
      } catch (error) {
        console.error('Failed to mark as read:', error);
      }
    }
  };

  const handleNewMessage = (message) => {
    setMessages(prev => {
      // Avoid duplicates by checking if message already exists
      if (prev.some(m => m.id === message.id)) {
        return prev;
      }
      return [...prev, message];
    });

    // Update conversation list
    setConversations(prev =>
      prev.map(conv =>
        conv.id === message.conversationId
          ? { ...conv, lastMessage: message, updatedAt: message.createdAt }
          : conv
      ).sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
    );

    // Auto-mark as read if conversation is active
    if (currentConversation?.id === message.conversationId) {
      markAsRead(message.id);
    }
  };

  const handleMessageStatus = (message) => {
    setMessages(prev => 
      prev.map(msg => 
        msg.id === message.id ? { ...msg, status: message.status } : msg
      )
    );
  };

  const handleTypingIndicator = (data) => {
    setTypingUsers(prev => {
      const newSet = new Set(prev);
      if (data.typing) {
        newSet.add(data.username);
      } else {
        newSet.delete(data.username);
      }
      return newSet;
    });

    // Clear typing indicator after timeout
    if (data.typing) {
      setTimeout(() => {
        setTypingUsers(prev => {
          const newSet = new Set(prev);
          newSet.delete(data.username);
          return newSet;
        });
      }, 3000);
    }
  };

  const clearChat = async () => {
    if (!currentConversation) return;
    
    try {
      await messageAPI.clearChat(currentConversation.id);
      setMessages([]);
    } catch (error) {
      console.error('Failed to clear chat:', error);
    }
  };

  return {
    conversations,
    currentConversation,
    messages,
    typingUsers,
    loading,
    selectConversation,
    sendMessage,
    sendTypingIndicator,
    createDirectConversation,
    markAsRead,
    loadConversations,
    clearChat,
  };
};