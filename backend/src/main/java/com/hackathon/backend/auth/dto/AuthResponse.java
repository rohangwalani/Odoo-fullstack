package com.hackathon.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standardized API response for all auth endpoints.
 *
 * Normal success   → { success:true,  message, userId, username, email }
 * 2FA pending      → { success:false, requires2FA:true, pendingUserId, message }
 * Failure          → { success:false, message }
 *
 * @JsonInclude(NON_NULL) ensures null fields are omitted from JSON output.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private String email;
    private String token;       // Reserved for JWT — null for now

    // 2FA pending fields
    private Boolean requires2FA;
    private Long pendingUserId;

    // Private constructor — use static factory methods
    private AuthResponse() {}

    /** Factory: success response with full user details */
    public static AuthResponse success(String message, Long userId, String username, String email) {
        AuthResponse r = new AuthResponse();
        r.success = true;
        r.message = message;
        r.userId = userId;
        r.username = username;
        r.email = email;
        return r;
    }

    /** Factory: simple success message (no user data) */
    public static AuthResponse success(String message) {
        AuthResponse r = new AuthResponse();
        r.success = true;
        r.message = message;
        return r;
    }

    /** Factory: failure response */
    public static AuthResponse failure(String message) {
        AuthResponse r = new AuthResponse();
        r.success = false;
        r.message = message;
        return r;
    }

    /**
     * Factory: 2FA pending response.
     * Returned by POST /api/auth/login when the user has 2FA enabled.
     * The client must then POST to /api/auth/verify-otp with pendingUserId + OTP.
     */
    public static AuthResponse pending2FA(Long userId) {
        AuthResponse r = new AuthResponse();
        r.success = false;
        r.requires2FA = true;
        r.pendingUserId = userId;
        r.message = "Two-factor authentication required. A 6-digit code has been sent to your email.";
        return r;
    }

    // Getters
    public boolean isSuccess()       { return success; }
    public String getMessage()       { return message; }
    public Long getUserId()          { return userId; }
    public String getUsername()      { return username; }
    public String getEmail()         { return email; }
    public String getToken()         { return token; }
    public Boolean getRequires2FA()  { return requires2FA; }
    public Long getPendingUserId()   { return pendingUserId; }
}
