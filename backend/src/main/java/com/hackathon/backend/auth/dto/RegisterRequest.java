package com.hackathon.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration requests.
 * Keeps the User entity decoupled from API input.
 *
 * Validation rules:
 * - username: 3–50 characters, only letters, digits, and underscores
 * - email:    standard email format
 * - password: min 8 chars; requires uppercase, lowercase, digit, and special character
 */
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Username may only contain letters, digits, and underscores"
    )
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    /**
     * Password strength rules (OWASP-aligned):
     * - At least 8 characters
     * - At least one uppercase letter (A-Z)
     * - At least one lowercase letter (a-z)
     * - At least one digit (0-9)
     * - At least one special character (@$!%*?&#^+=)
     * - Maximum 72 characters (bcrypt input limit)
     */
    @NotBlank(message = "Password is required")
    @Size(max = 72, message = "Password must not exceed 72 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^+=])[A-Za-z\\d@$!%*?&#^+=]{8,72}$",
        message = "Password must be at least 8 characters and include uppercase, lowercase, digit, and special character (@$!%*?&#^+=)"
    )
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
