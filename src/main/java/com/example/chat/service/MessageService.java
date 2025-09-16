package com.example.chat.service;

import com.example.chat.dto.MessageDto;
import com.example.chat.dto.SendMessageRequest;
import com.example.chat.model.Conversation;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.repository.ConversationMemberRepository;
import com.example.chat.repository.ConversationRepository;
import com.example.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository memberRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private EventPublisher eventPublisher;

    @Autowired
    private PresenceService presenceService;

    private static final String OFFLINE_MESSAGES_KEY = "offline_messages:";

    @Transactional
    public MessageDto sendMessage(SendMessageRequest request, Long senderId) {
        // Verify user is member of conversation
        if (!memberRepository.existsByConversationIdAndUserId(request.getConversationId(), senderId)) {
            throw new RuntimeException("Access denied");
        }

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User sender = userService.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setType(request.getType());
        message.setContent(request.getContent());
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setMimeType(request.getMimeType());

        message = messageRepository.save(message);

        // Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageDto messageDto = new MessageDto(message);

        // Send real-time message to online users
        sendRealTimeMessage(conversation.getId(), messageDto);

        // Publish to Redis for real-time delivery across instances
        if (eventPublisher != null) {
            eventPublisher.publishMessageToRedis(conversation.getId(), messageDto);

            // Publish message event to Kafka
            eventPublisher.publishMessageEvent(new com.example.chat.dto.events.MessageEvent(
                com.example.chat.dto.events.MessageEvent.Type.MESSAGE_SENT,
                message.getId(),
                conversation.getId(),
                senderId,
                sender.getUsername(),
                message.getContent(),
                message.getType(),
                message.getStatus()
            ));
        }

        // Handle offline message delivery
        handleOfflineMessageDelivery(conversation.getId(), messageDto, senderId);

        return messageDto;
    }

    @Transactional
    public void updateMessageStatus(Long messageId, Message.Status status, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Verify user is member of conversation
        if (!memberRepository.existsByConversationIdAndUserId(message.getConversation().getId(), userId)) {
            throw new RuntimeException("Access denied");
        }

        message.setStatus(status);
        messageRepository.save(message);

        // Send status update to sender
        MessageDto messageDto = new MessageDto(message);
        messagingTemplate.convertAndSendToUser(
                message.getSender().getUsername(),
                "/queue/message-status",
                messageDto
        );

        // Publish status event to Kafka
        com.example.chat.dto.events.MessageEvent.Type eventType = switch (status) {
            case DELIVERED -> com.example.chat.dto.events.MessageEvent.Type.MESSAGE_DELIVERED;
            case READ -> com.example.chat.dto.events.MessageEvent.Type.MESSAGE_READ;
            default -> null;
        };
        
        if (eventType != null && eventPublisher != null) {
            eventPublisher.publishMessageEvent(new com.example.chat.dto.events.MessageEvent(
                eventType,
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getType(),
                status
            ));
        }
    }

    public void sendTypingIndicator(Long conversationId, Long userId, boolean typing) {
        // Verify user is member of conversation
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new RuntimeException("Access denied");
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Broadcast typing indicator via WebSocket
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/typing",
            new TypingIndicator(user.getUsername(), typing));

        // Publish typing indicator to Redis for multi-instance support
        if (eventPublisher != null) {
            eventPublisher.publishTypingToRedis(conversationId, userId, user.getUsername(), typing);

            // Publish typing event to Kafka
            com.example.chat.dto.events.PresenceEvent.Type eventType = typing ?
                com.example.chat.dto.events.PresenceEvent.Type.USER_TYPING :
                com.example.chat.dto.events.PresenceEvent.Type.USER_STOP_TYPING;

            eventPublisher.publishPresenceEvent(new com.example.chat.dto.events.PresenceEvent(
                eventType, userId, user.getUsername(), conversationId
            ));
        }
    }

    private void sendRealTimeMessage(Long conversationId, MessageDto messageDto) {
        // Send to conversation topic
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, messageDto);

        // Mark as delivered for online users
        List<Long> onlineMembers = getOnlineMembers(conversationId);
        if (!onlineMembers.isEmpty()) {
            messageDto.setStatus(Message.Status.DELIVERED);
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/status", messageDto);
        }
    }

    private void handleOfflineMessageDelivery(Long conversationId, MessageDto messageDto, Long senderId) {
        // Get offline members
        List<Long> offlineMembers = getOfflineMembers(conversationId, senderId);
        
        for (Long memberId : offlineMembers) {
            String key = OFFLINE_MESSAGES_KEY + memberId;
            redisTemplate.opsForList().rightPush(key, messageDto);
            redisTemplate.expire(key, 7, TimeUnit.DAYS); // Keep for 7 days
        }
    }

    public void deliverOfflineMessages(Long userId) {
        String key = OFFLINE_MESSAGES_KEY + userId;
        List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
        
        if (messages != null && !messages.isEmpty()) {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            for (Object messageObj : messages) {
                messagingTemplate.convertAndSendToUser(
                        user.getUsername(),
                        "/queue/offline-messages",
                        messageObj
                );
            }
            
            // Clear offline messages
            redisTemplate.delete(key);
        }
    }

    private List<Long> getOnlineMembers(Long conversationId) {
        return memberRepository.findByConversationId(conversationId).stream()
                .map(member -> member.getUser().getId())
                .filter(presenceService::isUserOnline)
                .toList();
    }

    private List<Long> getOfflineMembers(Long conversationId, Long excludeUserId) {
        return memberRepository.findOtherMembers(conversationId, excludeUserId).stream()
                .map(member -> member.getUser().getId())
                .filter(userId -> !presenceService.isUserOnline(userId))
                .toList();
    }

    @Transactional
    public void clearChat(Long conversationId, Long userId) {
        // Verify user is member of conversation
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new RuntimeException("Access denied");
        }

        // Delete all messages in the conversation
        messageRepository.deleteByConversationId(conversationId);

        // Update conversation timestamp
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Notify all members that chat was cleared
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/cleared", 
                Map.of("clearedBy", userId, "timestamp", LocalDateTime.now()));
    }

    public static class TypingIndicator {
        private String username;
        private boolean typing;

        public TypingIndicator(String username, boolean typing) {
            this.username = username;
            this.typing = typing;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public boolean isTyping() { return typing; }
        public void setTyping(boolean typing) { this.typing = typing; }
    }
}