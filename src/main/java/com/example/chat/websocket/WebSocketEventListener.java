package com.example.chat.websocket;

import com.example.chat.model.User;
import com.example.chat.service.MessageService;
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

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null) {
            logger.info("User connected: {}", user.getName());
            
            // Set user online and deliver offline messages
            User userEntity = userService.findByUsername(user.getName()).orElse(null);
            if (userEntity != null) {
                userService.setUserOnline(userEntity.getId());
                messageService.deliverOfflineMessages(userEntity.getId());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null) {
            logger.info("User disconnected: {}", user.getName());
            
            // Set user offline
            User userEntity = userService.findByUsername(user.getName()).orElse(null);
            if (userEntity != null) {
                userService.setUserOffline(userEntity.getId());
            }
        }
    }
}