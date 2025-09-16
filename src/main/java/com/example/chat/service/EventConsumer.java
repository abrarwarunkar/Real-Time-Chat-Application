package com.example.chat.service;

import com.example.chat.dto.events.MessageEvent;
import com.example.chat.dto.events.PresenceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.kafka.events.enabled", havingValue = "true")
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = "chat.message.events", groupId = "chat-analytics")
    public void handleMessageEvent(MessageEvent event) {
        logger.info("Processing message event: {} for message: {}", 
                   event.getEventType(), event.getMessageId());
        
        // Process message events for analytics
        switch (event.getEventType()) {
            case MESSAGE_SENT:
                processMessageSent(event);
                break;
            case MESSAGE_DELIVERED:
                processMessageDelivered(event);
                break;
            case MESSAGE_READ:
                processMessageRead(event);
                break;
            case MESSAGE_DELETED:
                processMessageDeleted(event);
                break;
        }
    }

    @KafkaListener(topics = "chat.user.events", groupId = "chat-analytics")
    public void handlePresenceEvent(PresenceEvent event) {
        logger.info("Processing presence event: {} for user: {}", 
                   event.getEventType(), event.getUserId());
        
        // Process presence events for analytics
        switch (event.getEventType()) {
            case USER_ONLINE:
                processUserOnline(event);
                break;
            case USER_OFFLINE:
                processUserOffline(event);
                break;
            case USER_TYPING:
                processUserTyping(event);
                break;
            case USER_STOP_TYPING:
                processUserStopTyping(event);
                break;
        }
    }

    private void processMessageSent(MessageEvent event) {
        // Analytics: Track message volume, conversation activity
        logger.debug("Message sent in conversation: {}", event.getConversationId());
    }

    private void processMessageDelivered(MessageEvent event) {
        // Analytics: Track delivery rates, response times
        logger.debug("Message delivered: {}", event.getMessageId());
    }

    private void processMessageRead(MessageEvent event) {
        // Analytics: Track read rates, engagement metrics
        logger.debug("Message read: {}", event.getMessageId());
    }

    private void processMessageDeleted(MessageEvent event) {
        // Analytics: Track deletion patterns
        logger.debug("Message deleted: {}", event.getMessageId());
    }

    private void processUserOnline(PresenceEvent event) {
        // Analytics: Track user activity patterns, peak hours
        logger.debug("User online: {}", event.getUserId());
    }

    private void processUserOffline(PresenceEvent event) {
        // Analytics: Track session duration, usage patterns
        logger.debug("User offline: {}", event.getUserId());
    }

    private void processUserTyping(PresenceEvent event) {
        // Analytics: Track engagement, conversation dynamics
        logger.debug("User typing in conversation: {}", event.getConversationId());
    }

    private void processUserStopTyping(PresenceEvent event) {
        // Analytics: Track typing patterns
        logger.debug("User stopped typing in conversation: {}", event.getConversationId());
    }
}