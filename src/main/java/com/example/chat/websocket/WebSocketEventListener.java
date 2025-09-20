package com.example.chat.websocket;

import com.example.chat.model.User;
import com.example.chat.service.MessageService;
import com.example.chat.service.PresenceService;
import com.example.chat.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            Principal user = headerAccessor.getUser();
            String sessionId = headerAccessor.getSessionId();

            if (user != null) {
                logger.info("WebSocket connection established - User: {}, Session: {}", user.getName(), sessionId);

                // Set user online and deliver offline messages
                try {
                    User userEntity = userService.findByUsername(user.getName()).orElse(null);
                    if (userEntity != null) {
                        presenceService.setUserOnline(userEntity.getId(), userEntity.getUsername());
                        logger.debug("User {} set online successfully", user.getName());
                    } else {
                        logger.warn("User entity not found for username: {}", user.getName());
                    }
                } catch (Exception e) {
                    logger.error("Error setting user online: {}", user.getName(), e);
                }
            } else {
                logger.warn("WebSocket connection without authenticated user - Session: {}", sessionId);
            }
        } catch (Exception e) {
            logger.error("Error handling WebSocket connect event", e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            Principal user = headerAccessor.getUser();
            String sessionId = headerAccessor.getSessionId();

            if (user != null) {
                logger.info("WebSocket disconnection - User: {}, Session: {}", user.getName(), sessionId);

                // Set user offline
                try {
                    User userEntity = userService.findByUsername(user.getName()).orElse(null);
                    if (userEntity != null) {
                        presenceService.setUserOffline(userEntity.getId(), userEntity.getUsername());
                        logger.debug("User {} set offline successfully", user.getName());
                    } else {
                        logger.warn("User entity not found for username: {}", user.getName());
                    }
                } catch (Exception e) {
                    logger.error("Error setting user offline: {}", user.getName(), e);
                }
            } else {
                logger.info("Anonymous WebSocket disconnection - Session: {}", sessionId);
            }
        } catch (Exception e) {
            logger.error("Error handling WebSocket disconnect event", e);
        }
    }
}