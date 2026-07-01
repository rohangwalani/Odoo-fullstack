package com.hackathon.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standardized API response for all auth endpoints.
 * Keeps all auth responses consistent — easy to consume on the frontend.
 *
 * @JsonInclude(NON_NULL) ensures null fields (like token) are omitted from JSON output.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private String email;
    private String token; // Reserved for JWT — null for now

    // Private constructor — use static factory methods
    private AuthResponse() {}

    /** Factory: success response with user details */
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

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
}
