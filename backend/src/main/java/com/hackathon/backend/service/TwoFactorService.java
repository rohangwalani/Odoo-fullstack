package com.hackathon.backend.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TwoFactorService {

    private static final int OTP_DIGITS = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(2);
    private static final String OTP_KEY_PREFIX = "otp:";

    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expiry = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public TwoFactorService() {
    }

    public String generateAndStoreOtp(Long userId) {
        int rawOtp = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(rawOtp);
        String key = OTP_KEY_PREFIX + userId;
        store.put(key, otp);
        expiry.put(key, System.currentTimeMillis() + OTP_TTL.toMillis());
        return otp;
    }

    public boolean verifyOtp(Long userId, String otp) {
        String key = OTP_KEY_PREFIX + userId;
        if (isExpired(key)) {
            store.remove(key);
            expiry.remove(key);
            return false;
        }
        String stored = store.get(key);
        if (stored != null && stored.equals(otp)) {
            store.remove(key);
            expiry.remove(key);
            return true;
        }
        return false;
    }

    public void invalidateOtp(Long userId) {
        String key = OTP_KEY_PREFIX + userId;
        store.remove(key);
        expiry.remove(key);
    }

    public long getRemainingTtlSeconds(Long userId) {
        String key = OTP_KEY_PREFIX + userId;
        if (isExpired(key)) return -2L;
        Long exp = expiry.get(key);
        return exp != null ? (exp - System.currentTimeMillis()) / 1000 : -2L;
    }

    private boolean isExpired(String key) {
        Long exp = expiry.get(key);
        return exp == null || System.currentTimeMillis() > exp;
    }
}
