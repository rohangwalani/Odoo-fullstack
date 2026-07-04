package com.hackathon.backend.auth;

import com.hackathon.backend.auth.dto.AuthResponse;
import com.hackathon.backend.auth.dto.CompanySignupRequest;
import com.hackathon.backend.auth.dto.LoginRequest;
import com.hackathon.backend.auth.dto.VerifyOtpRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/company/signup")
    public ResponseEntity<AuthResponse> registerCompany(@Valid @ModelAttribute CompanySignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerCompany(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<AuthResponse> enableTwoFactor(@RequestBody Map<String, Long> body) {
        Long employeeId = body.get("employeeId");
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(AuthResponse.failure("employeeId is required"));
        }
        return ResponseEntity.ok(authService.enableTwoFactor(employeeId));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<AuthResponse> disableTwoFactor(@RequestBody Map<String, Long> body) {
        Long employeeId = body.get("employeeId");
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(AuthResponse.failure("employeeId is required"));
        }
        return ResponseEntity.ok(authService.disableTwoFactor(employeeId));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        return ResponseEntity.ok(authService.logout());
    }

    @PutMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @Valid @RequestBody com.hackathon.backend.auth.dto.ChangePasswordRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        com.hackathon.backend.security.CustomUserDetails userDetails = 
            (com.hackathon.backend.security.CustomUserDetails) authentication.getPrincipal();
            
        return ResponseEntity.ok(authService.changePassword(userDetails.getId(), request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(
            @Valid @RequestBody com.hackathon.backend.auth.dto.ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(
            @Valid @RequestBody com.hackathon.backend.auth.dto.ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
