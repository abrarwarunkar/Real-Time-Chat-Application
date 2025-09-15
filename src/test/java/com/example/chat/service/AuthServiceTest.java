package com.example.chat.service;

import com.example.chat.dto.AuthRequest;
import com.example.chat.dto.AuthResponse;
import com.example.chat.dto.RegisterRequest;
import com.example.chat.model.User;
import com.example.chat.repository.RefreshTokenRepository;
import com.example.chat.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "test@example.com", "password123");
        authRequest = new AuthRequest("testuser", "password123");
        user = new User("testuser", "test@example.com", "encodedPassword");
        user.setId(1L);
    }

    @Test
    void register_Success() {
        when(userService.existsByUsername("testuser")).thenReturn(false);
        when(userService.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userService.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateAccessToken(user)).thenReturn("accessToken");
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    void register_UsernameExists_ThrowsException() {
        when(userService.existsByUsername("testuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateAccessToken(user)).thenReturn("accessToken");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }
}