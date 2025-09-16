package com.example.chat.dto;

import com.example.chat.model.Message;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class MessageStatusDto {
    private Long messageId;
    private Long conversationId;
    private Message.Status status;
    private Long userId;
    private String username;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public MessageStatusDto() {}

    public MessageStatusDto(Long messageId, Long conversationId, Message.Status status, Long userId, String username) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.status = status;
        this.userId = userId;
        this.username = username;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Message.Status getStatus() { return status; }
    public void setStatus(Message.Status status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}