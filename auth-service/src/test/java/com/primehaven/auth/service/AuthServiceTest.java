package com.primehaven.auth.service;

import com.primehaven.auth.dto.AuthResponse;
import com.primehaven.auth.dto.LoginRequest;
import com.primehaven.auth.dto.RegisterRequest;
import com.primehaven.auth.entity.Role;
import com.primehaven.auth.entity.User;
import com.primehaven.auth.repository.UserRepository;
import com.primehaven.auth.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    private RegisterRequest register(String u, String p, Role r) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(u); req.setPassword(p); req.setRole(r);
        return req;
    }

    private LoginRequest login(String u, String p) {
        LoginRequest req = new LoginRequest();
        req.setUsername(u); req.setPassword(p);
        return req;
    }

    @Test
    void register_success_hashesPasswordAndReturnsToken() {
        when(userRepository.existsByUsername("ravi")).thenReturn(false);
        when(passwordEncoder.encode("pw123")).thenReturn("HASHED");
        when(jwtUtil.generateToken("ravi", "CLIENT")).thenReturn("tok");

        AuthResponse res = authService.register(register("ravi", "pw123", Role.CLIENT));

        assertEquals("tok", res.getToken());
        assertEquals("CLIENT", res.getRole());
        verify(userRepository).save(argThat((User u) -> u.getPassword().equals("HASHED")));
    }

    @Test
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsername("ravi")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(register("ravi", "pw", Role.AGENT)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        User user = new User("ravi", "HASHED", Role.AGENT);
        when(userRepository.findByUsername("ravi")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pw", "HASHED")).thenReturn(true);
        when(jwtUtil.generateToken("ravi", "AGENT")).thenReturn("tok");

        AuthResponse res = authService.login(login("ravi", "pw"));
        assertEquals("AGENT", res.getRole());
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        when(userRepository.findByUsername("ravi")).thenReturn(Optional.of(new User("ravi", "HASHED", Role.CLIENT)));
        when(passwordEncoder.matches("bad", "HASHED")).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> authService.login(login("ravi", "bad")));
    }

    @Test
    void login_unknownUser_throwsBadCredentials() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> authService.login(login("ghost", "x")));
    }
}
