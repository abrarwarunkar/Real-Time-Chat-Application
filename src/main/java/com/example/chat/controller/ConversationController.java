package com.example.chat.controller;

import com.example.chat.dto.ConversationDto;
import com.example.chat.dto.MessageDto;
import com.example.chat.model.User;
import com.example.chat.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<ConversationDto>> getUserConversations(Authentication auth) {
        User user = (User) auth.getPrincipal();
        List<ConversationDto> conversations = conversationService.getUserConversations(user.getId());
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        return conversationService.getConversation(id, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/direct")
    public ResponseEntity<ConversationDto> createDirectConversation(
            @RequestBody Map<String, Long> request, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Long otherUserId = request.get("userId");
        
        ConversationDto conversation = conversationService.createDirectConversation(user.getId(), otherUserId);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationDto> createGroupConversation(
            @RequestBody Map<String, Object> request, Authentication auth) {
        User user = (User) auth.getPrincipal();
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<Long> memberIds = (List<Long>) request.get("memberIds");
        
        ConversationDto conversation = conversationService.createGroupConversation(name, user.getId(), memberIds);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageDto>> getConversationMessages(
            @PathVariable Long id, Pageable pageable, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Page<MessageDto> messages = conversationService.getConversationMessages(id, user.getId(), pageable);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable Long id, @RequestBody Map<String, Long> request, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Long newMemberId = request.get("userId");
        
        conversationService.addMemberToGroup(id, user.getId(), newMemberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id, @RequestBody Map<String, Long> request, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Long messageId = request.get("messageId");
        
        conversationService.markAsRead(id, user.getId(), messageId);
        return ResponseEntity.ok().build();
    }
}