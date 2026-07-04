package com.hackathon.backend.service;

import com.hackathon.backend.dto.AttendanceResponse;
import com.hackathon.backend.exception.DuplicateAttendanceException;
import com.hackathon.backend.model.Attendance;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.repository.AttendanceRepository;
import com.hackathon.backend.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Test");
        employee.setLastName("User");
        employee.setEmail("test@example.com");
    }

    @Test
    void checkIn_Successful() {
        when(employeeRepository.findByEmail("test@example.com")).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndDate(eq(employee), any(LocalDate.class))).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> {
            Attendance a = i.getArgument(0);
            a.setId(100L);
            return a;
        });

        AttendanceResponse res = attendanceService.checkIn("test@example.com");

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("Test User", res.getEmployeeName());
        assertEquals("PRESENT", res.getStatus());
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void checkIn_Duplicate_ThrowsException() {
        when(employeeRepository.findByEmail("test@test.com")).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeAndDate(eq(employee), any(LocalDate.class)))
                .thenReturn(Optional.of(new Attendance()));

        assertThrows(DuplicateAttendanceException.class, () -> attendanceService.checkIn("test@test.com"));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void checkIn_UserNotFound_ThrowsException() {
        when(employeeRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> attendanceService.checkIn("notfound@test.com"));
    }
}
