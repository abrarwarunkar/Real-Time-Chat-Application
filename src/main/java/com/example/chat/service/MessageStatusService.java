package com.example.chat.service;

import com.example.chat.dto.MessageStatusDto;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class MessageStatusService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private EventPublisher eventPublisher;

    private static final String MESSAGE_STATUS_KEY = "message_status:";

    @Transactional
    public void markAsDelivered(Long messageId, Long userId, String username) {
        updateMessageStatus(messageId, Message.Status.DELIVERED, userId, username);
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId, String username) {
        updateMessageStatus(messageId, Message.Status.READ, userId, username);
    }

    @Transactional
    public void markConversationAsRead(Long conversationId, Long userId, String username) {
        messageRepository.findUnreadMessagesByConversationAndNotSender(conversationId, userId)
                .forEach(message -> {
                    message.setStatus(Message.Status.READ);
                    messageRepository.save(message);
                    
                    MessageStatusDto statusDto = new MessageStatusDto(
                        message.getId(), conversationId, Message.Status.READ, userId, username
                    );
                    
                    // Notify sender
                    messagingTemplate.convertAndSendToUser(
                        message.getSender().getUsername(),
                        "/queue/message-status",
                        statusDto
                    );
                    
                    // Cache status
                    cacheMessageStatus(message.getId(), Message.Status.READ, userId);
                });
    }

    private void updateMessageStatus(Long messageId, Message.Status status, Long userId, String username) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null || message.getSender().getId().equals(userId)) {
            return; // Don't update status for sender's own messages
        }

        message.setStatus(status);
        messageRepository.save(message);

        MessageStatusDto statusDto = new MessageStatusDto(
            messageId, message.getConversation().getId(), status, userId, username
        );

        // Notify sender via WebSocket
        messagingTemplate.convertAndSendToUser(
            message.getSender().getUsername(),
            "/queue/message-status",
            statusDto
        );

        // Publish to Redis for multi-instance support
        if (eventPublisher != null) {
            eventPublisher.publishMessageToRedis(message.getConversation().getId(), statusDto);
        }

        // Cache status in Redis
        cacheMessageStatus(messageId, status, userId);
    }

    private void cacheMessageStatus(Long messageId, Message.Status status, Long userId) {
        String key = MESSAGE_STATUS_KEY + messageId + ":" + userId;
        redisTemplate.opsForValue().set(key, status.name(), 24, TimeUnit.HOURS);
    }

    public Message.Status getCachedMessageStatus(Long messageId, Long userId) {
        String key = MESSAGE_STATUS_KEY + messageId + ":" + userId;
        String status = (String) redisTemplate.opsForValue().get(key);
        return status != null ? Message.Status.valueOf(status) : null;
    }
}