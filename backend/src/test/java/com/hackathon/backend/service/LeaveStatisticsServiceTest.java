package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveStatisticsDTO;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LeaveStatisticsServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private LeaveStatisticsService leaveStatisticsService;

    @Test
    void testGetStatistics() {
        when(leaveRequestRepository.count()).thenReturn(100L);
        when(leaveRequestRepository.countByStatus(LeaveStatus.PENDING)).thenReturn(10L);
        when(leaveRequestRepository.countByStatus(LeaveStatus.APPROVED)).thenReturn(80L);
        when(leaveRequestRepository.countByStatus(LeaveStatus.REJECTED)).thenReturn(10L);

        LeaveStatisticsDTO dto = leaveStatisticsService.getStatistics();

        assertEquals(100L, dto.getTotalRequests());
        assertEquals(10L, dto.getPendingRequests());
        assertEquals(80L, dto.getApprovedRequests());
        assertEquals(10L, dto.getRejectedRequests());
    }
}
