package com.hackathon.backend.profile.exception;

public class AvatarUploadException extends RuntimeException {
    public AvatarUploadException(String message) {
        super(message);
    }
    
    public AvatarUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
