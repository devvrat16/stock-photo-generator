package com.mockai.service;

import com.mockai.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> users = new ConcurrentHashMap<>();

    public AuthService(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        // Default user
        users.put("admin", passwordEncoder.encode("password"));
    }

    public String authenticate(String username, String password) {
        String encodedPassword = users.get(username);
        if (encodedPassword != null && passwordEncoder.matches(password, encodedPassword)) {
            return jwtUtil.generateToken(username);
        }
        return null;
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, passwordEncoder.encode(password));
        return true;
    }
}
