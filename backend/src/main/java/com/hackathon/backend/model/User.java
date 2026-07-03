package com.hackathon.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * User entity mapped to the `users` table.
 * email and username are unique constraints to prevent duplicate registrations.
 * twoFactorEnabled controls whether OTP verification is required on login.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_users_email"),
        @UniqueConstraint(columnNames = "username", name = "uk_users_username")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Column(nullable = false, unique = true)
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    /**
     * When true, login requires OTP verification sent to the user's email.
     * Default: false (2FA disabled on registration).
     * Users can toggle this via POST /api/auth/2fa/enable or /api/auth/2fa/disable.
     */
    @Column(name = "two_factor_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean twoFactorEnabled = false;

    // Constructors

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
}
