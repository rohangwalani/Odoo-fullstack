package com.hackathon.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Manages 2FA OTP generation, storage, and verification using Redis.
 *
 * Key pattern : otp:{userId}
 * Value       : 6-digit numeric code (e.g. "483921")
 * TTL         : 2 minutes (configurable via OTP_TTL constant)
 *
 * Security choices:
 * - SecureRandom (cryptographically strong PRNG — NOT Math.random())
 * - 6-digit code: 10^6 = 1,000,000 possibilities (vs 10^4 = 10,000 for 4-digit)
 * - One-time use: code is deleted from Redis on successful verification
 * - Rate limiting enforced externally by RateLimitService
 */
@Service
public class TwoFactorService {

    private static final int OTP_DIGITS = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(2);
    private static final String OTP_KEY_PREFIX = "otp:";

    private final StringRedisTemplate redis;
    private final SecureRandom secureRandom = new SecureRandom();

    public TwoFactorService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Generates a 6-digit OTP, stores it in Redis with a 2-minute TTL,
     * and returns it so the caller can send it to the user.
     *
     * Calling this again for the same userId overwrites any existing OTP
     * (useful for "Resend OTP" functionality).
     *
     * @param userId the authenticated user's ID
     * @return the generated OTP string (exactly 6 digits, zero-padded)
     */
    public String generateAndStoreOtp(Long userId) {
        // nextInt(900000) gives 0–899999; +100000 gives 100000–999999 (always 6 digits)
        int rawOtp = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(rawOtp);
        redis.opsForValue().set(OTP_KEY_PREFIX + userId, otp, OTP_TTL);
        return otp;
    }

    /**
     * Verifies the OTP for a user.
     * On SUCCESS: deletes the OTP key from Redis (one-time use).
     * On FAILURE: leaves the key intact so the user can retry (subject to rate limiting).
     *
     * @param userId  the user's ID
     * @param otp     the code the user submitted
     * @return true if the OTP matches and hasn't expired; false otherwise
     */
    public boolean verifyOtp(Long userId, String otp) {
        String stored = redis.opsForValue().get(OTP_KEY_PREFIX + userId);
        if (stored != null && stored.equals(otp)) {
            redis.delete(OTP_KEY_PREFIX + userId);
            return true;
        }
        return false;
    }

    /**
     * Deletes any active OTP for a user (e.g. when 2FA is disabled).
     */
    public void invalidateOtp(Long userId) {
        redis.delete(OTP_KEY_PREFIX + userId);
    }

    /**
     * Returns the remaining TTL in seconds for a user's OTP.
     * Returns -2 if the key does not exist (expired or never set).
     */
    public long getRemainingTtlSeconds(Long userId) {
        Long ttl = redis.getExpire(OTP_KEY_PREFIX + userId, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2L;
    }
}
