package com.example.chat.dto.events;

import com.example.chat.model.Message;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class MessageEvent {
    
    public enum Type {
        MESSAGE_SENT, MESSAGE_DELIVERED, MESSAGE_READ, MESSAGE_DELETED
    }

    private Type eventType;
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private Message.Type messageType;
    private Message.Status status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public MessageEvent() {}

    public MessageEvent(Type eventType, Long messageId, Long conversationId, Long senderId, 
                       String senderUsername, String content, Message.Type messageType, Message.Status status) {
        this.eventType = eventType;
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.messageType = messageType;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Type getEventType() { return eventType; }
    public void setEventType(Type eventType) { this.eventType = eventType; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Message.Type getMessageType() { return messageType; }
    public void setMessageType(Message.Type messageType) { this.messageType = messageType; }

    public Message.Status getStatus() { return status; }
    public void setStatus(Message.Status status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}