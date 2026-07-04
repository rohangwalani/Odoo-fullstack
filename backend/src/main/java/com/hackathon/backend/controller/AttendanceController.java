package com.hackathon.backend.controller;

import com.hackathon.backend.dto.AttendanceResponse;
import com.hackathon.backend.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(Authentication authentication) {
        try {
            AttendanceResponse res = attendanceService.checkIn(authentication.getName());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/checkout")
    public ResponseEntity<?> checkOut(Authentication authentication) {
        try {
            AttendanceResponse res = attendanceService.checkOut(authentication.getName());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendance(Authentication authentication) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(authentication.getName()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance() {
        // In a real app, secure this endpoint for ADMIN only
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }
    
    @GetMapping("/{employeeId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployeeId(employeeId));
    }
}
