package com.example.chat.dto;

import com.example.chat.model.Conversation;
import java.time.LocalDateTime;
import java.util.List;

public class ConversationDto {
    private Long id;
    private Conversation.Type type;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UserDto> members;
    private MessageDto lastMessage;
    private long unreadCount;

    public ConversationDto() {}

    public ConversationDto(Conversation conversation) {
        this.id = conversation.getId();
        this.type = conversation.getType();
        this.name = conversation.getName();
        this.createdAt = conversation.getCreatedAt();
        this.updatedAt = conversation.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conversation.Type getType() { return type; }
    public void setType(Conversation.Type type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<UserDto> getMembers() { return members; }
    public void setMembers(List<UserDto> members) { this.members = members; }

    public MessageDto getLastMessage() { return lastMessage; }
    public void setLastMessage(MessageDto lastMessage) { this.lastMessage = lastMessage; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
}