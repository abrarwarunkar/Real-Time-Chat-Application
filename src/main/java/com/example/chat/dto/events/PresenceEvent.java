package com.example.chat.dto.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class PresenceEvent {
    
    public enum Type {
        USER_ONLINE, USER_OFFLINE, USER_TYPING, USER_STOP_TYPING
    }

    private Type eventType;
    private Long userId;
    private String username;
    private Long conversationId;
    private boolean online;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public PresenceEvent() {}

    public PresenceEvent(Type eventType, Long userId, String username, boolean online) {
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
        this.online = online;
        this.timestamp = LocalDateTime.now();
    }

    public PresenceEvent(Type eventType, Long userId, String username, Long conversationId) {
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
        this.conversationId = conversationId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Type getEventType() { return eventType; }
    public void setEventType(Type eventType) { this.eventType = eventType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}