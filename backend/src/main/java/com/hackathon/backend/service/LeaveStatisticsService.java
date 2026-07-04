package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveStatisticsDTO;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class LeaveStatisticsService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveStatisticsService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public LeaveStatisticsDTO getStatistics() {
        LeaveStatisticsDTO dto = new LeaveStatisticsDTO();
        dto.setTotalRequests(leaveRequestRepository.count());
        dto.setPendingRequests(leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        dto.setApprovedRequests(leaveRequestRepository.countByStatus(LeaveStatus.APPROVED));
        dto.setRejectedRequests(leaveRequestRepository.countByStatus(LeaveStatus.REJECTED));
        return dto;
    }
}
