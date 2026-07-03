package com.hackathon.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for the OTP verification step of 2FA login.
 *
 * Flow:
 * 1. User logs in → receives {requires2FA: true, pendingUserId: 123}
 * 2. User checks email for 6-digit OTP
 * 3. User POSTs this DTO to /api/auth/verify-otp
 */
public class VerifyOtpRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
    private String otp;

    public VerifyOtpRequest() {}

    public VerifyOtpRequest(Long userId, String otp) {
        this.userId = userId;
        this.otp = otp;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
