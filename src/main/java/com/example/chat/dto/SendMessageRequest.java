package com.example.chat.dto;

import com.example.chat.model.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SendMessageRequest {
    @NotNull
    private Long conversationId;
    
    private Message.Type type = Message.Type.TEXT;
    
    private String content;
    
    private String attachmentUrl;
    
    private String mimeType;

    public SendMessageRequest() {}

    public SendMessageRequest(Long conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
    }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Message.Type getType() { return type; }
    public void setType(Message.Type type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}