package com.hackathon.backend.auth;

import com.hackathon.backend.auth.dto.AuthResponse;
import com.hackathon.backend.auth.dto.LoginRequest;
import com.hackathon.backend.auth.dto.RegisterRequest;
import com.hackathon.backend.auth.dto.VerifyOtpRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller.
 *
 * Endpoints:
 *   POST /api/auth/register        — register new user
 *   POST /api/auth/login           — login (returns 2FA pending if 2FA enabled)
 *   POST /api/auth/verify-otp      — submit OTP for 2FA login
 *   POST /api/auth/2fa/enable      — enable 2FA for a user
 *   POST /api/auth/2fa/disable     — disable 2FA for a user
 *
 * All validation via @Valid + GlobalExceptionHandler.
 * All business logic in AuthService.
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
     * Registers a new user. Returns 201 Created on success.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * POST /api/auth/login
     *
     * - If 2FA disabled: returns 200 OK with full user details.
     * - If 2FA enabled:  returns 200 OK with {requires2FA:true, pendingUserId}.
     *   Client must then call POST /api/auth/verify-otp.
     * - If rate limited: returns 429 Too Many Requests.
     * - If bad credentials: returns 401 Unauthorized.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/verify-otp
     *
     * Step 2 of 2FA login. Validates the 6-digit OTP sent to the user's email.
     * - Valid OTP: returns 200 OK with full user details.
     * - Invalid/expired OTP: returns 401 Unauthorized.
     * - Rate limited (3 wrong attempts): returns 429 Too Many Requests.
     *
     * Request body: { "userId": 1, "otp": "483921" }
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    /**
     * POST /api/auth/2fa/enable
     *
     * Enables 2FA for a user. Next login will require OTP verification.
     * Request body: { "userId": 1 }
     *
     * Note: In production, protect this with JWT authentication.
     */
    @PostMapping("/2fa/enable")
    public ResponseEntity<AuthResponse> enableTwoFactor(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body(AuthResponse.failure("userId is required"));
        }
        return ResponseEntity.ok(authService.enableTwoFactor(userId));
    }

    /**
     * POST /api/auth/2fa/disable
     *
     * Disables 2FA for a user. Also invalidates any active OTP in Redis.
     * Request body: { "userId": 1 }
     */
    @PostMapping("/2fa/disable")
    public ResponseEntity<AuthResponse> disableTwoFactor(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body(AuthResponse.failure("userId is required"));
        }
        return ResponseEntity.ok(authService.disableTwoFactor(userId));
    }
}
