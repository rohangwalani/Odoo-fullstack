package com.hackathon.backend.auth;

import com.hackathon.backend.auth.dto.AuthResponse;
import com.hackathon.backend.auth.dto.LoginRequest;
import com.hackathon.backend.auth.dto.RegisterRequest;
import com.hackathon.backend.auth.dto.VerifyOtpRequest;
import com.hackathon.backend.auth.exception.RateLimitExceededException;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.UserRepository;
import com.hackathon.backend.service.EmailService;
import com.hackathon.backend.service.RateLimitService;
import com.hackathon.backend.service.TwoFactorService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core authentication service.
 * Handles registration, login (with optional 2FA), OTP verification, and 2FA toggle.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TwoFactorService twoFactorService;
    private final RateLimitService rateLimitService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       TwoFactorService twoFactorService,
                       RateLimitService rateLimitService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.twoFactorService = twoFactorService;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Registers a new user.
     * Validates uniqueness of email and username, hashes the password,
     * and sends a welcome email via Mailtrap.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("This username is already taken.");
        }

        User user = new User(
                request.getUsername().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword())
        );
        User savedUser = userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
        } catch (Exception e) {
            System.err.println("[EmailService] Welcome email failed for " + savedUser.getEmail() + ": " + e.getMessage());
        }

        return AuthResponse.success(
                "Registration successful! Welcome, " + savedUser.getUsername() + ".",
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail()
        );
    }

    /**
     * Authenticates a user.
     *
     * Rate limiting: max 5 failed attempts per 5 minutes per email address.
     *
     * If 2FA is DISABLED: returns a full success response immediately.
     * If 2FA is ENABLED:  generates + stores OTP in Redis (TTL=2min),
     *                     sends OTP email, returns {requires2FA:true, pendingUserId}.
     */
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Rate limit check — before any DB query to avoid timing attacks
        if (rateLimitService.isLoginRateLimited(email)) {
            throw new RateLimitExceededException(
                    "Too many failed login attempts. Please wait 5 minutes before trying again."
            );
        }

        // 2. Find user — generic error message to prevent email enumeration
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            rateLimitService.recordLoginAttempt(email);
            return new IllegalArgumentException("Invalid email or password.");
        });

        // 3. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            rateLimitService.recordLoginAttempt(email);
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // 4. Successful credential check — clear failed attempt counter
        rateLimitService.clearLoginAttempts(email);

        // 5. 2FA check
        if (user.isTwoFactorEnabled()) {
            String otp = twoFactorService.generateAndStoreOtp(user.getId());
            try {
                emailService.sendTwoFactorEmail(user.getEmail(), user.getUsername(), otp);
            } catch (Exception e) {
                System.err.println("[EmailService] 2FA OTP email failed for " + user.getEmail() + ": " + e.getMessage());
            }
            return AuthResponse.pending2FA(user.getId());
        }

        // 6. No 2FA — return full success
        return AuthResponse.success(
                "Login successful. Welcome back, " + user.getUsername() + "!",
                user.getId(), user.getUsername(), user.getEmail()
        );
    }

    /**
     * Verifies the OTP submitted by the user for 2FA login.
     *
     * Rate limiting: max 3 attempts per 2-minute window per userId.
     * One-time use: OTP is deleted from Redis on successful verification.
     */
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        Long userId = request.getUserId();

        if (rateLimitService.isOtpRateLimited(userId)) {
            throw new RateLimitExceededException(
                    "Too many OTP attempts. Please request a new code and try again."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session. Please log in again."));

        rateLimitService.recordOtpAttempt(userId);

        if (!twoFactorService.verifyOtp(userId, request.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired OTP. Please check your email and try again.");
        }

        // Success — clear rate limit counters
        rateLimitService.clearOtpAttempts(userId);

        return AuthResponse.success(
                "Login successful. Welcome back, " + user.getUsername() + "!",
                user.getId(), user.getUsername(), user.getEmail()
        );
    }

    /**
     * Enables 2FA for a user.
     * In production this would require JWT auth; kept simple for hackathon.
     */
    @Transactional
    public AuthResponse enableTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        return AuthResponse.success("Two-factor authentication has been enabled for your account.");
    }

    /**
     * Disables 2FA for a user and invalidates any active OTP.
     */
    @Transactional
    public AuthResponse disableTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setTwoFactorEnabled(false);
        twoFactorService.invalidateOtp(userId);
        userRepository.save(user);
        return AuthResponse.success("Two-factor authentication has been disabled for your account.");
    }
}
