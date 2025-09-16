package com.example.chat.service;

import com.example.chat.dto.events.PresenceEvent;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class PresenceService {

    private static final Logger logger = LoggerFactory.getLogger(PresenceService.class);
    private static final String ONLINE_USERS_KEY = "online_users";
    private static final String USER_LAST_SEEN_KEY = "user_last_seen:";
    private static final long PRESENCE_TIMEOUT = 300; // 5 minutes

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private EventPublisher eventPublisher;

    @Autowired
    private MessageService messageService;

    public void setUserOnline(Long userId, String username) {
        try {
            // Add to online users set
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
            
            // Set expiration for auto-cleanup
            redisTemplate.expire(ONLINE_USERS_KEY, PRESENCE_TIMEOUT, TimeUnit.SECONDS);
            
            // Update last seen
            redisTemplate.opsForValue().set(USER_LAST_SEEN_KEY + userId, LocalDateTime.now().toString());
            
            // Publish presence event
            if (eventPublisher != null) {
                eventPublisher.publishPresenceToRedis(userId, username, true);
                eventPublisher.publishPresenceEvent(new PresenceEvent(PresenceEvent.Type.USER_ONLINE, userId, username, true));
            }
            
            // Deliver offline messages
            messageService.deliverOfflineMessages(userId);
            
            logger.debug("User {} is now online", username);
        } catch (Exception e) {
            logger.error("Error setting user online", e);
        }
    }

    public void setUserOffline(Long userId, String username) {
        try {
            // Remove from online users set
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
            
            // Update last seen in database
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setLastSeen(LocalDateTime.now());
                userRepository.save(user);
            }
            
            // Publish presence event
            if (eventPublisher != null) {
                eventPublisher.publishPresenceToRedis(userId, username, false);
                eventPublisher.publishPresenceEvent(new PresenceEvent(PresenceEvent.Type.USER_OFFLINE, userId, username, false));
            }
            
            logger.debug("User {} is now offline", username);
        } catch (Exception e) {
            logger.error("Error setting user offline", e);
        }
    }

    public boolean isUserOnline(Long userId) {
        try {
            return redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId.toString());
        } catch (Exception e) {
            logger.error("Error checking user online status", e);
            return false;
        }
    }

    public Set<Object> getOnlineUsers() {
        try {
            return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
        } catch (Exception e) {
            logger.error("Error getting online users", e);
            return Set.of();
        }
    }

    public void heartbeat(Long userId) {
        try {
            if (isUserOnline(userId)) {
                // Refresh presence timeout
                redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
                redisTemplate.expire(ONLINE_USERS_KEY, PRESENCE_TIMEOUT, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error processing heartbeat", e);
        }
    }

    public LocalDateTime getLastSeen(Long userId) {
        try {
            String lastSeenStr = (String) redisTemplate.opsForValue().get(USER_LAST_SEEN_KEY + userId);
            if (lastSeenStr != null) {
                return LocalDateTime.parse(lastSeenStr);
            }
            
            // Fallback to database
            User user = userRepository.findById(userId).orElse(null);
            return user != null ? user.getLastSeen() : null;
        } catch (Exception e) {
            logger.error("Error getting last seen", e);
            return null;
        }
    }
}