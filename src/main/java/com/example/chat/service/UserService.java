package com.example.chat.service;

import com.example.chat.dto.UserDto;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRESENCE_KEY_PREFIX = "presence:";
    private static final long PRESENCE_TIMEOUT = 300; // 5 minutes

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserDto> searchUsers(String query) {
        return userRepository.searchUsers(query).stream()
                .map(user -> {
                    UserDto dto = new UserDto(user);
                    dto.setOnline(isUserOnline(user.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void setUserOnline(Long userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, "online", PRESENCE_TIMEOUT, TimeUnit.SECONDS);
        
        // Update last seen
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public void setUserOffline(Long userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        
        // Update last seen
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public boolean isUserOnline(Long userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public UserDto getUserPresence(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            UserDto dto = new UserDto(userOpt.get());
            dto.setOnline(isUserOnline(userId));
            return dto;
        }
        return null;
    }
}