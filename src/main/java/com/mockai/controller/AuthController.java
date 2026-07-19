package com.mockai.controller;

import com.mockai.model.AuthRequest;
import com.mockai.model.AuthResponse;
import com.mockai.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String token = authService.authenticate(request.getUsername(), request.getPassword());
        if (token != null) {
            return ResponseEntity.ok(new AuthResponse(token, request.getUsername(), "Login successful"));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (request.getUsername() == null || request.getUsername().length() < 3) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username must be at least 3 characters"));
        }
        if (request.getPassword() == null || request.getPassword().length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 4 characters"));
        }

        boolean registered = authService.register(request.getUsername(), request.getPassword());
        if (registered) {
            return ResponseEntity.ok(Map.of("message", "Registration successful"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
    }
}
