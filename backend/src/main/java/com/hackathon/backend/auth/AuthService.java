package com.hackathon.backend.auth;

import com.hackathon.backend.auth.dto.AuthResponse;
import com.hackathon.backend.auth.dto.LoginRequest;
import com.hackathon.backend.auth.dto.RegisterRequest;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.UserRepository;
import com.hackathon.backend.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core authentication service.
 * Handles user registration and login logic.
 * Fully self-contained in the auth package — minimal coupling with other features.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Registers a new user.
     * Checks for duplicate email and username before saving.
     * Sends a welcome email via Mailtrap on success.
     *
     * @param request the registration data (username, email, password)
     * @return AuthResponse with user details on success
     * @throws IllegalArgumentException if email or username already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // Check for duplicate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("This username is already taken.");
        }

        // Build and save the new user with hashed password
        User user = new User(
                request.getUsername().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword())
        );
        User savedUser = userRepository.save(user);

        // Send welcome email (non-blocking failure: if email fails, registration still succeeds)
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
        } catch (Exception e) {
            // Log email failure but don't roll back registration
            System.err.println("[EmailService] Failed to send welcome email to " + savedUser.getEmail() + ": " + e.getMessage());
        }

        return AuthResponse.success(
                "Registration successful! Welcome, " + savedUser.getUsername() + ".",
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    /**
     * Authenticates an existing user.
     * Verifies email exists and BCrypt password matches.
     *
     * @param request the login credentials (email, password)
     * @return AuthResponse with user details on success
     * @throws IllegalArgumentException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return AuthResponse.success(
                "Login successful. Welcome back, " + user.getUsername() + "!",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
