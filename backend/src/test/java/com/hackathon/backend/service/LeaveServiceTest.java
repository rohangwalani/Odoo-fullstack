package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveRequestDTO;
import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.exception.InvalidLeaveException;
import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.model.LeaveType;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.LeaveRequestRepository;
import com.hackathon.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaveService leaveService;

    private User user;
    private LeaveRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setUsername("Test User");

        requestDTO = new LeaveRequestDTO();
        requestDTO.setLeaveType("SICK");
        requestDTO.setFromDate(LocalDate.now().plusDays(1));
        requestDTO.setToDate(LocalDate.now().plusDays(2));
        requestDTO.setRemarks("Sick");
    }

    @Test
    void applyForLeave_Successful() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(leaveRequestRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> {
            LeaveRequest r = i.getArgument(0);
            r.setId(10L);
            r.setStatus(LeaveStatus.PENDING);
            return r;
        });

        LeaveResponseDTO res = leaveService.applyForLeave("test@test.com", requestDTO);

        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals("SICK", res.getLeaveType());
        assertEquals("PENDING", res.getStatus());
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    @Test
    void applyForLeave_PastDates_ThrowsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        requestDTO.setFromDate(LocalDate.now().minusDays(1));

        assertThrows(InvalidLeaveException.class, () -> leaveService.applyForLeave("test@test.com", requestDTO));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void applyForLeave_OverlappingApproved_ThrowsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        LeaveRequest existing = new LeaveRequest();
        existing.setStatus(LeaveStatus.APPROVED);
        existing.setFromDate(LocalDate.now().plusDays(1));
        existing.setToDate(LocalDate.now().plusDays(3));

        when(leaveRequestRepository.findByUser(user)).thenReturn(Collections.singletonList(existing));

        assertThrows(InvalidLeaveException.class, () -> leaveService.applyForLeave("test@test.com", requestDTO));
        verify(leaveRequestRepository, never()).save(any());
    }
}
