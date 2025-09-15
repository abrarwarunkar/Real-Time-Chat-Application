package com.example.chat.controller;

import com.example.chat.dto.UserDto;
import com.example.chat.model.User;
import com.example.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication auth) {
        User user = (User) auth.getPrincipal();
        UserDto userDto = new UserDto(user);
        userDto.setOnline(userService.isUserOnline(user.getId()));
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}/presence")
    public ResponseEntity<UserDto> getUserPresence(@PathVariable Long id) {
        UserDto userDto = userService.getUserPresence(id);
        if (userDto != null) {
            return ResponseEntity.ok(userDto);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<UserDto> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
}