package com.example.chat.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class SecurityAuditService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditService.class);
    private static final String FAILED_LOGIN_KEY = "failed_login:";
    private static final String SUSPICIOUS_ACTIVITY_KEY = "suspicious:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void recordFailedLogin(String identifier) {
        String key = FAILED_LOGIN_KEY + identifier;
        Long attempts = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
        
        logger.warn("Failed login attempt {} for identifier: {}", attempts, identifier);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(identifier);
        }
    }

    public void recordSuccessfulLogin(String identifier) {
        String key = FAILED_LOGIN_KEY + identifier;
        redisTemplate.delete(key);
        logger.info("Successful login for identifier: {}", identifier);
    }

    public boolean isAccountLocked(String identifier) {
        String key = FAILED_LOGIN_KEY + identifier;
        Long attempts = (Long) redisTemplate.opsForValue().get(key);
        return attempts != null && attempts >= MAX_FAILED_ATTEMPTS;
    }

    private void lockAccount(String identifier) {
        String key = SUSPICIOUS_ACTIVITY_KEY + identifier;
        redisTemplate.opsForValue().set(key, LocalDateTime.now().toString(), LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
        logger.error("Account locked due to suspicious activity: {}", identifier);
    }

    public void recordSuspiciousActivity(String identifier, String activity) {
        logger.warn("Suspicious activity detected for {}: {}", identifier, activity);
    }
}