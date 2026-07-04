package com.hackathon.backend.exception;

public class InvalidLeaveException extends RuntimeException {
    public InvalidLeaveException(String message) {
        super(message);
    }
}
