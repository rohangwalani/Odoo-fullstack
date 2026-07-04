package com.hackathon.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ApprovalNotAllowedException extends RuntimeException {
    public ApprovalNotAllowedException(String message) {
        super(message);
    }
}
