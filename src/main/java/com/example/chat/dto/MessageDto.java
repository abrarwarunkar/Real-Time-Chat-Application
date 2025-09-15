package com.example.chat.dto;

import com.example.chat.model.Message;
import java.time.LocalDateTime;

public class MessageDto {
    private Long id;
    private Long conversationId;
    private UserDto sender;
    private Message.Type type;
    private String content;
    private String attachmentUrl;
    private String mimeType;
    private Message.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private boolean deleted;

    public MessageDto() {}

    public MessageDto(Message message) {
        this.id = message.getId();
        this.conversationId = message.getConversation().getId();
        this.sender = new UserDto(message.getSender());
        this.type = message.getType();
        this.content = message.getContent();
        this.attachmentUrl = message.getAttachmentUrl();
        this.mimeType = message.getMimeType();
        this.status = message.getStatus();
        this.createdAt = message.getCreatedAt();
        this.editedAt = message.getEditedAt();
        this.deleted = message.isDeleted();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public UserDto getSender() { return sender; }
    public void setSender(UserDto sender) { this.sender = sender; }

    public Message.Type getType() { return type; }
    public void setType(Message.Type type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Message.Status getStatus() { return status; }
    public void setStatus(Message.Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}