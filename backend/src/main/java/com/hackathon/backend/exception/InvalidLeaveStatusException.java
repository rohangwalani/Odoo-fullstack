package com.hackathon.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLeaveStatusException extends RuntimeException {
    public InvalidLeaveStatusException(String message) {
        super(message);
    }
}
