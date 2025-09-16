package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    @Autowired
    private PresenceService presenceService;

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(Authentication auth) {
        User user = (User) auth.getPrincipal();
        presenceService.heartbeat(user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/online")
    public ResponseEntity<Set<Object>> getOnlineUsers() {
        Set<Object> onlineUsers = presenceService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable Long userId) {
        boolean online = presenceService.isUserOnline(userId);
        LocalDateTime lastSeen = presenceService.getLastSeen(userId);
        
        return ResponseEntity.ok(Map.of(
            "online", online,
            "lastSeen", lastSeen != null ? lastSeen.toString() : null
        ));
    }
}