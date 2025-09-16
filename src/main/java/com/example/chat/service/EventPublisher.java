package com.example.chat.service;

import com.example.chat.dto.events.MessageEvent;
import com.example.chat.dto.events.PresenceEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.kafka.events.enabled", havingValue = "true")
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishMessageToRedis(Long conversationId, Object messageData) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("conversationId", conversationId);
            payload.put("messageData", messageData);
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend("chat.messages", jsonPayload);
            
            logger.debug("Published message to Redis for conversation: {}", conversationId);
        } catch (JsonProcessingException e) {
            logger.error("Error publishing message to Redis", e);
        }
    }

    public void publishPresenceToRedis(Long userId, String username, boolean online) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("username", username);
            payload.put("online", online);
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend("user.presence", jsonPayload);
            
            logger.debug("Published presence update to Redis for user: {} - {}", username, online ? "online" : "offline");
        } catch (JsonProcessingException e) {
            logger.error("Error publishing presence to Redis", e);
        }
    }

    public void publishTypingToRedis(Long conversationId, Long userId, String username, boolean typing) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("conversationId", conversationId);
            payload.put("userId", userId);
            payload.put("username", username);
            payload.put("typing", typing);
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend("chat.typing", jsonPayload);
            
            logger.debug("Published typing indicator to Redis for user: {} in conversation: {}", username, conversationId);
        } catch (JsonProcessingException e) {
            logger.error("Error publishing typing indicator to Redis", e);
        }
    }

    public void publishMessageEvent(MessageEvent event) {
        try {
            kafkaTemplate.send("chat.message.events", event.getMessageId().toString(), event);
            logger.debug("Published message event to Kafka: {}", event.getEventType());
        } catch (Exception e) {
            logger.error("Error publishing message event to Kafka", e);
        }
    }

    public void publishPresenceEvent(PresenceEvent event) {
        try {
            kafkaTemplate.send("chat.user.events", event.getUserId().toString(), event);
            logger.debug("Published presence event to Kafka: {}", event.getEventType());
        } catch (Exception e) {
            logger.error("Error publishing presence event to Kafka", e);
        }
    }
}