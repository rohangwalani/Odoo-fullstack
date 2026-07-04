package com.hackathon.backend.controller;

import com.hackathon.backend.model.Employee;
import com.hackathon.backend.repository.EmployeeRepository;
import com.hackathon.backend.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetController(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        Optional<Employee> employeeOptional = employeeRepository.findByEmail(email.toLowerCase());
        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            String token = UUID.randomUUID().toString();
            employee.setResetToken(token);
            employee.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            employeeRepository.save(employee);

            // Construct the reset link to point to your frontend
            String resetLink = "http://localhost:3000/reset-password?token=" + token;
            
            try {
                emailService.sendPasswordResetEmail(employee.getEmail(), resetLink);
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

        Optional<Employee> employeeOptional = employeeRepository.findByResetToken(token);
        if (employeeOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }

        Employee employee = employeeOptional.get();
        if (employee.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }

        employee.setPassword(passwordEncoder.encode(newPassword));
        employee.setTemporaryPassword(false);
        employee.setResetToken(null);
        employee.setResetTokenExpiry(null);
        employeeRepository.save(employee);

        return ResponseEntity.ok("Password successfully reset");
    }
}
