package com.hackathon.backend.service;

import com.hackathon.backend.dto.AttendanceResponse;
import com.hackathon.backend.model.Attendance;
import com.hackathon.backend.model.AttendanceStatus;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.AttendanceRepository;
import com.hackathon.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    public AttendanceResponse checkIn(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate today = LocalDate.now();

        if (attendanceRepository.findByUserAndDate(user, today).isPresent()) {
            throw new RuntimeException("Already checked in today");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(today);
        attendance.setCheckIn(LocalTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT); // Optimistic default

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    public AttendanceResponse checkOut(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByUserAndDate(user, today)
                .orElseThrow(() -> new RuntimeException("No check-in found for today"));

        if (attendance.getCheckOut() != null) {
            throw new RuntimeException("Already checked out today");
        }

        attendance.setCheckOut(LocalTime.now());
        // Simple logic for half day: if worked less than 4 hours
        long hoursWorked = java.time.Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toHours();
        if (hoursWorked < 4) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    public List<AttendanceResponse> getMyAttendance(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return attendanceRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAllAttendance() {
        return attendanceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<AttendanceResponse> getAttendanceByEmployeeId(Long employeeId) {
        User user = userRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("User not found"));
        return attendanceRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        return new AttendanceResponse(
                a.getId(),
                a.getUser().getId(),
                a.getUser().getUsername(),
                a.getDate(),
                a.getCheckIn(),
                a.getCheckOut(),
                a.getStatus() != null ? a.getStatus().name() : null
        );
    }
}
