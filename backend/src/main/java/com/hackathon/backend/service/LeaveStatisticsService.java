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
        long pending = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long approved = leaveRequestRepository.countByStatus(LeaveStatus.APPROVED);
        long rejected = leaveRequestRepository.countByStatus(LeaveStatus.REJECTED);
        long totalMonth = leaveRequestRepository.countTotalLeavesThisMonth();

        return new LeaveStatisticsDTO(pending, approved, rejected, totalMonth);
    }
}
