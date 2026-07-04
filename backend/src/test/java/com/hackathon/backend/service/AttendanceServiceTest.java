package com.hackathon.backend.service;

import com.hackathon.backend.dto.AttendanceResponse;
import com.hackathon.backend.exception.DuplicateAttendanceException;
import com.hackathon.backend.model.Attendance;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.AttendanceRepository;
import com.hackathon.backend.repository.UserRepository;
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
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setUsername("Test User");
    }

    @Test
    void checkIn_Successful() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(attendanceRepository.findByUserAndDate(eq(user), any(LocalDate.class))).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> {
            Attendance a = i.getArgument(0);
            a.setId(100L);
            return a;
        });

        AttendanceResponse res = attendanceService.checkIn("test@test.com");

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("Test User", res.getEmployeeName());
        assertEquals("PRESENT", res.getStatus());
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void checkIn_Duplicate_ThrowsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(attendanceRepository.findByUserAndDate(eq(user), any(LocalDate.class)))
                .thenReturn(Optional.of(new Attendance()));

        assertThrows(DuplicateAttendanceException.class, () -> attendanceService.checkIn("test@test.com"));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void checkIn_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> attendanceService.checkIn("notfound@test.com"));
    }
}
