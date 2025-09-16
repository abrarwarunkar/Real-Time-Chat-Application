package com.example.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RedisMessageSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String messageBody = new String(message.getBody());
            
            logger.debug("Received Redis message on channel: {} with body: {}", channel, messageBody);
            
            Map<String, Object> messageData = objectMapper.readValue(messageBody, Map.class);
            
            switch (channel) {
                case "chat.messages":
                    handleChatMessage(messageData);
                    break;
                case "user.presence":
                    handlePresenceUpdate(messageData);
                    break;
                case "chat.typing":
                    handleTypingIndicator(messageData);
                    break;
                default:
                    logger.warn("Unknown channel: {}", channel);
            }
        } catch (Exception e) {
            logger.error("Error processing Redis message", e);
        }
    }

    private void handleChatMessage(Map<String, Object> messageData) {
        Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, messageData);
    }

    private void handlePresenceUpdate(Map<String, Object> presenceData) {
        String username = (String) presenceData.get("username");
        Boolean online = (Boolean) presenceData.get("online");
        
        messagingTemplate.convertAndSend("/topic/presence", presenceData);
        
        if (online) {
            // Deliver offline messages when user comes online
            Long userId = Long.valueOf(presenceData.get("userId").toString());
            // This will be handled by MessageService.deliverOfflineMessages
        }
    }

    private void handleTypingIndicator(Map<String, Object> typingData) {
        Long conversationId = Long.valueOf(typingData.get("conversationId").toString());
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/typing", typingData);
    }
}