package com.hackathon.backend.service;

import com.hackathon.backend.dto.AttendanceResponse;
import com.hackathon.backend.model.Attendance;
import com.hackathon.backend.model.AttendanceStatus;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.repository.AttendanceRepository;
import com.hackathon.backend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hackathon.backend.exception.DuplicateAttendanceException;
import com.hackathon.backend.exception.AttendanceNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    public AttendanceResponse checkIn(String email) {
        log.info("Checking in user with email: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        LocalDate today = LocalDate.now();

        if (attendanceRepository.findByEmployeeAndDate(employee, today).isPresent()) {
            log.warn("User {} already checked in today", email);
            throw new DuplicateAttendanceException("Already checked in today");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckIn(LocalTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT); // Optimistic default

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    public AttendanceResponse checkOut(String email) {
        log.info("Checking out user with email: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeAndDate(employee, today)
                .orElseThrow(() -> new AttendanceNotFoundException("No check-in found for today"));

        if (attendance.getCheckOut() != null) {
            log.warn("User {} already checked out today", email);
            throw new DuplicateAttendanceException("Already checked out today");
        }

        attendance.setCheckOut(LocalTime.now());
        long minutesWorked = java.time.Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
        double hoursWorked = minutesWorked / 60.0;
        attendance.setWorkingHours(hoursWorked);

        // Simple logic for half day: if worked less than 4 hours
        if (hoursWorked < 4.0) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    public List<AttendanceResponse> getMyAttendance(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return attendanceRepository.findByEmployee(employee).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAllAttendance() {
        return attendanceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<AttendanceResponse> getAttendanceByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("User not found"));
        return attendanceRepository.findByEmployee(employee).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        return new AttendanceResponse(
                a.getId(),
                a.getEmployee().getId(),
                a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName(),
                a.getDate(),
                a.getCheckIn(),
                a.getCheckOut(),
                a.getStatus() != null ? a.getStatus().name() : null,
                a.getWorkingHours()
        );
    }
}
