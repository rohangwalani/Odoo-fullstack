package com.hackathon.backend.auth.exception;

import com.hackathon.backend.auth.dto.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler — intercepts exceptions from all controllers
 * and returns clean, consistent JSON error responses instead of Spring's default HTML error pages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles @Valid annotation failures (missing/invalid fields).
     * Returns a map of fieldName -> errorMessage.
     * HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Validation failed. Please check the highlighted fields.");
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles business logic errors (duplicate email, wrong password, user not found, etc.)
     * Distinguishes 409 Conflict (duplicates) from 401 Unauthorized (bad credentials).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage();
        HttpStatus status = (msg != null && (msg.contains("already exists") || msg.contains("already taken")))
                ? HttpStatus.CONFLICT
                : HttpStatus.UNAUTHORIZED;

        return ResponseEntity.status(status).body(AuthResponse.failure(msg));
    }

    /**
     * Handles rate limit violations.
     * HTTP 429 Too Many Requests.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<AuthResponse> handleRateLimit(RateLimitExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(AuthResponse.failure(ex.getMessage()));
    }

    /**
     * Catch-all for unexpected server errors. HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception ex) {
        System.err.println("[GlobalExceptionHandler] Unhandled exception: " + ex.getClass().getSimpleName() + " — " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.failure("An unexpected error occurred. Please try again."));
    }
}
