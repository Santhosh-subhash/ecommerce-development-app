package com.example.ecommerce.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, String> data) {

        User user = authService.register(data);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponse(user, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> data,
            HttpSession session) {

        User user = authService.authenticate(
                data.get("username"),
                data.get("password"));

        session.setAttribute("userId", user.getUserId());

        return ResponseEntity.ok(
                userResponse(user, "Login successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> currentUser(
            HttpSession session) {

        Object userId = session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in"));
        }

        User user = authService.findUserById(
                Long.valueOf(userId.toString()));

        return ResponseEntity.ok(userResponse(user, "Session active"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                Map.of("message", "Logout successful"));
    }

    private Map<String, Object> userResponse(
            User user, String message) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("message", message);
        result.put("userId", user.getUserId());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("username", user.getUsername());

        return result;
    }
}