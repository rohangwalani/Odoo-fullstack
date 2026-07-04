package com.hackathon.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class HRExceptionHandler {

    @ExceptionHandler(DuplicateAttendanceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateAttendance(DuplicateAttendanceException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidLeaveException.class)
    public ResponseEntity<Map<String, String>> handleInvalidLeave(InvalidLeaveException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            AttendanceNotFoundException.class,
            LeaveNotFoundException.class,
            PayrollNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFoundExceptions(RuntimeException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SalaryNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSalaryNotFound(SalaryNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidSalaryException.class)
    public ResponseEntity<Map<String, String>> handleInvalidSalary(InvalidSalaryException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PayrollCalculationException.class)
    public ResponseEntity<Map<String, String>> handlePayrollCalculationException(PayrollCalculationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleAuthExceptions(Exception ex) {
        return buildErrorResponse("Invalid email or password.", HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("status", status.toString());
        return new ResponseEntity<>(response, status);
    }
}
