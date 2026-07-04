package com.hackathon.backend.auth.exception;

/**
 * Thrown when a user exceeds the allowed number of attempts
 * for login or OTP verification within the rate-limit window.
 *
 * Caught by GlobalExceptionHandler and mapped to HTTP 429 Too Many Requests.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
