package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveApprovalDTO;
import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.exception.InvalidLeaveStatusException;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.model.LeaveType;
import com.hackathon.backend.repository.AttendanceRepository;
import com.hackathon.backend.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveApprovalServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private LeaveApprovalService leaveApprovalService;

    private LeaveRequest leaveRequest;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");

        leaveRequest = new LeaveRequest();
        leaveRequest.setId(100L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(LeaveType.SICK);
        leaveRequest.setFromDate(LocalDate.now().plusDays(1));
        leaveRequest.setToDate(LocalDate.now().plusDays(2));
        leaveRequest.setStatus(LeaveStatus.PENDING);
    }

    @Test
    void testApproveLeave_Success() {
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        
        LeaveApprovalDTO dto = new LeaveApprovalDTO();
        dto.setComments("Approved!");
        
        LeaveResponseDTO response = leaveApprovalService.approveLeave(100L, dto);
        
        assertEquals("APPROVED", response.getStatus());
        assertEquals("Approved!", response.getAdminComments());
        
        verify(attendanceRepository, times(2)).save(any());
        verify(notificationService, times(1)).sendNotification(eq(employee), anyString());
    }

    @Test
    void testApproveLeave_InvalidStatus() {
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        
        assertThrows(InvalidLeaveStatusException.class, () -> {
            leaveApprovalService.approveLeave(100L, null);
        });
    }

    @Test
    void testRejectLeave_Success() {
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        
        LeaveApprovalDTO dto = new LeaveApprovalDTO();
        dto.setComments("Rejected!");
        
        LeaveResponseDTO response = leaveApprovalService.rejectLeave(100L, dto);
        
        assertEquals("REJECTED", response.getStatus());
        assertEquals("Rejected!", response.getAdminComments());
        
        verify(attendanceRepository, never()).save(any());
        verify(notificationService, times(1)).sendNotification(eq(employee), anyString());
    }
}
