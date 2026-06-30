package com.hackathon.backend.controller;

import com.hackathon.backend.model.User;
import com.hackathon.backend.service.EmailService;
import com.hackathon.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private final UserService userService;
    private final EmailService emailService;

    public PasswordResetController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = UUID.randomUUID().toString();
            userService.saveResetToken(user, token);

            // Construct the reset link to point to your frontend
            String resetLink = "http://localhost:3000/reset-password?token=" + token;
            
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Failed to send email");
            }
        }
        
        // Always return OK to prevent email enumeration attacks
        return ResponseEntity.ok("If an account with that email exists, a reset link has been sent.");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Token and new password are required");
        }

        Optional<User> userOptional = userService.validateResetToken(token);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }

        User user = userOptional.get();
        userService.updatePassword(user, newPassword);

        return ResponseEntity.ok("Password successfully reset");
    }
}
