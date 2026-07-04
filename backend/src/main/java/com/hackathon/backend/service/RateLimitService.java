package com.hackathon.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed rate limiting service.
 *
 * Protects two attack surfaces:
 *
 * 1. Login endpoint  — prevents brute-force credential attacks
 *    Key   : rate:login:{email}
 *    Limit : 5 failed attempts per 5-minute window
 *
 * 2. OTP verify endpoint — prevents brute-force OTP guessing
 *    Key   : rate:otp:{userId}
 *    Limit : 3 attempts per 2-minute window (matches OTP TTL)
 *
 * Implementation:
 * - First attempt creates the key and sets TTL (sliding window NOT used — fixed window)
 * - Counter increments atomically via Redis INCR command
 * - On rate limit hit: caller throws RateLimitExceededException → 429 response
 * - On success: caller clears the counter so legitimate users aren't permanently blocked
 */
@Service
public class RateLimitService {

    // Login rate limit
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(5);
    private static final String LOGIN_RATE_PREFIX = "rate:login:";

    // OTP verification rate limit
    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final Duration OTP_WINDOW = Duration.ofMinutes(2);
    private static final String OTP_RATE_PREFIX = "rate:otp:";

    private final StringRedisTemplate redis;

    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // ─── Login Rate Limiting ──────────────────────────────────────────────────

    /**
     * @return true if the email has exceeded MAX_LOGIN_ATTEMPTS within the window
     */
    public boolean isLoginRateLimited(String email) {
        return isRateLimited(LOGIN_RATE_PREFIX + email.toLowerCase(), MAX_LOGIN_ATTEMPTS);
    }

    /**
     * Records one failed login attempt for the given email.
     * Sets TTL on first attempt; subsequent increments don't reset TTL.
     */
    public void recordLoginAttempt(String email) {
        increment(LOGIN_RATE_PREFIX + email.toLowerCase(), LOGIN_WINDOW);
    }

    /**
     * Clears the login attempt counter after a successful login.
     */
    public void clearLoginAttempts(String email) {
        redis.delete(LOGIN_RATE_PREFIX + email.toLowerCase());
    }

    // ─── OTP Rate Limiting ────────────────────────────────────────────────────

    /**
     * @return true if the userId has exceeded MAX_OTP_ATTEMPTS within the window
     */
    public boolean isOtpRateLimited(Long userId) {
        return isRateLimited(OTP_RATE_PREFIX + userId, MAX_OTP_ATTEMPTS);
    }

    /**
     * Records one OTP verification attempt for the given user.
     */
    public void recordOtpAttempt(Long userId) {
        increment(OTP_RATE_PREFIX + userId, OTP_WINDOW);
    }

    /**
     * Clears OTP attempt counter after successful verification.
     */
    public void clearOtpAttempts(Long userId) {
        redis.delete(OTP_RATE_PREFIX + userId);
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────

    private boolean isRateLimited(String key, int maxAttempts) {
        String val = redis.opsForValue().get(key);
        if (val == null) return false;
        return Integer.parseInt(val) >= maxAttempts;
    }

    private void increment(String key, Duration ttl) {
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            // First increment — set the expiry window
            redis.expire(key, ttl);
        }
    }
}
