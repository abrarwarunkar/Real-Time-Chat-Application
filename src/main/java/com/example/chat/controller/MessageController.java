package com.example.chat.controller;

import com.example.chat.dto.MessageDto;
import com.example.chat.dto.SendMessageRequest;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.service.MessageService;
import com.example.chat.service.MessageStatusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageStatusService messageStatusService;

    @PostMapping
    public java.util.concurrent.CompletableFuture<ResponseEntity<MessageDto>> sendMessage(@Valid @RequestBody SendMessageRequest request, Authentication auth) {
        User user = (User) auth.getPrincipal();
        return messageService.sendMessage(request, user.getId())
                .thenApply(message -> ResponseEntity.ok(message));
    }

    @PostMapping("/status")
    public ResponseEntity<Void> updateMessageStatus(@RequestBody Map<String, Object> request, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Long messageId = Long.valueOf(request.get("messageId").toString());
        Message.Status status = Message.Status.valueOf(request.get("status").toString());
        
        if (status == Message.Status.DELIVERED) {
            messageStatusService.markAsDelivered(messageId, user.getId(), user.getUsername());
        } else if (status == Message.Status.READ) {
            messageStatusService.markAsRead(messageId, user.getId(), user.getUsername());
        }
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markConversationAsRead(@PathVariable Long conversationId, Authentication auth) {
        User user = (User) auth.getPrincipal();
        messageStatusService.markConversationAsRead(conversationId, user.getId(), user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clear/{conversationId}")
    public ResponseEntity<Void> clearChat(@PathVariable Long conversationId, Authentication auth) {
        User user = (User) auth.getPrincipal();
        messageService.clearChat(conversationId, user.getId());
        return ResponseEntity.ok().build();
    }
}

@Controller
class WebSocketMessageController {

    @Autowired
    private MessageService messageService;

    @MessageMapping("/conversations/{conversationId}/send")
    public void sendMessage(@DestinationVariable Long conversationId, 
                           @Payload SendMessageRequest request, 
                           Principal principal) {
        // Extract user ID from principal (you may need to implement this based on your auth setup)
        // For now, assuming username is available and we can look up the user
        // In a real implementation, you'd store user ID in the principal or session
        
        // This is a simplified version - you'd need to implement proper user lookup
        messageService.sendMessage(request, getUserIdFromPrincipal(principal));
    }

    @MessageMapping("/conversations/{conversationId}/typing")
    public void sendTypingIndicator(@DestinationVariable Long conversationId,
                                   @Payload Map<String, Boolean> payload,
                                   Principal principal) {
        boolean typing = payload.get("typing");
        messageService.sendTypingIndicator(conversationId, getUserIdFromPrincipal(principal), typing);
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal instanceof org.springframework.security.core.Authentication) {
            org.springframework.security.core.Authentication auth = (org.springframework.security.core.Authentication) principal;
            com.example.chat.model.User user = (com.example.chat.model.User) auth.getPrincipal();
            return user.getId();
        }
        return 1L; // Fallback
    }
}