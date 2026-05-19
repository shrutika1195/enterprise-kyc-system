package com.internship.kyc_system.controller;

import com.internship.kyc_system.service.UserRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles authentication pages and user registration API.
 *
 * GET  /login     → serves login.html
 * GET  /register  → serves register.html
 * POST /api/auth/register → creates a new USER account
 */
@Controller
public class AuthController {

    private final UserRegistrationService registrationService;

    public AuthController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /** Serves the custom login page */
    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login.html"; // resolves to templates/login.html (Thymeleaf) OR static/login.html
    }

    /** Serves the registration page */
    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/register.html";
    }

    /**
     * POST /api/auth/register
     * Accepts JSON: { "username": "...", "password": "...", "firstName": "...", "lastName": "..." }
     * Returns 201 on success, 400 if username taken or validation fails.
     */
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.username().length() < 3) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Username must be at least 3 characters."));
        }
        if (request.password() == null || request.password().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password must be at least 6 characters."));
        }

        try {
            registrationService.registerUser(
                    request.username().toLowerCase().trim(),
                    request.password(),
                    request.firstName(),
                    request.lastName()
            );
            return ResponseEntity.status(201)
                    .body(Map.of("message", "Account created successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Simple record for the registration request body */
    public record RegisterRequest(
            String username,
            String password,
            String firstName,
            String lastName
    ) {}
}