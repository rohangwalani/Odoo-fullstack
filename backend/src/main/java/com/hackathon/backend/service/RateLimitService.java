package com.hackathon.backend.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(5);
    private static final String LOGIN_RATE_PREFIX = "rate:login:";

    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final Duration OTP_WINDOW = Duration.ofMinutes(2);
    private static final String OTP_RATE_PREFIX = "rate:otp:";

    private final ConcurrentHashMap<String, Integer> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expiry = new ConcurrentHashMap<>();

    public RateLimitService() {
    }

    public boolean isLoginRateLimited(String email) {
        return isRateLimited(LOGIN_RATE_PREFIX + email.toLowerCase(), MAX_LOGIN_ATTEMPTS);
    }

    public void recordLoginAttempt(String email) {
        increment(LOGIN_RATE_PREFIX + email.toLowerCase(), LOGIN_WINDOW);
    }

    public void clearLoginAttempts(String email) {
        String key = LOGIN_RATE_PREFIX + email.toLowerCase();
        store.remove(key);
        expiry.remove(key);
    }

    public boolean isOtpRateLimited(Long userId) {
        return isRateLimited(OTP_RATE_PREFIX + userId, MAX_OTP_ATTEMPTS);
    }

    public void recordOtpAttempt(Long userId) {
        increment(OTP_RATE_PREFIX + userId, OTP_WINDOW);
    }

    public void clearOtpAttempts(Long userId) {
        String key = OTP_RATE_PREFIX + userId;
        store.remove(key);
        expiry.remove(key);
    }

    private boolean isRateLimited(String key, int maxAttempts) {
        if (isExpired(key)) {
            store.remove(key);
            expiry.remove(key);
            return false;
        }
        Integer count = store.get(key);
        return count != null && count >= maxAttempts;
    }

    private void increment(String key, Duration ttl) {
        if (isExpired(key)) {
            store.remove(key);
            expiry.remove(key);
        }
        store.compute(key, (k, count) -> {
            if (count == null) {
                expiry.put(key, System.currentTimeMillis() + ttl.toMillis());
                return 1;
            }
            return count + 1;
        });
    }

    private boolean isExpired(String key) {
        Long exp = expiry.get(key);
        return exp != null && System.currentTimeMillis() > exp;
    }
}
