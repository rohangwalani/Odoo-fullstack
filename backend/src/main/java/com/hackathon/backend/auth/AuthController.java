package com.hackathon.backend.auth;

import com.hackathon.backend.auth.dto.AuthResponse;
import com.hackathon.backend.auth.dto.LoginRequest;
import com.hackathon.backend.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 * Exposes /api/auth/register and /api/auth/login endpoints.
 *
 * All input validation is handled by @Valid + GlobalExceptionHandler.
 * All business logic is in AuthService.
 * This controller is intentionally thin.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     *
     * Registers a new user account.
     * On success: 201 Created + user details + welcome email sent to Mailtrap.
     * On duplicate email/username: 409 Conflict.
     * On validation failure: 400 Bad Request with field errors.
     *
     * Request body:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "secret123"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     *
     * Authenticates a user with email and password.
     * On success: 200 OK + user details.
     * On invalid credentials: 401 Unauthorized.
     * On validation failure: 400 Bad Request.
     *
     * Request body:
     * {
     *   "email": "john@example.com",
     *   "password": "secret123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
