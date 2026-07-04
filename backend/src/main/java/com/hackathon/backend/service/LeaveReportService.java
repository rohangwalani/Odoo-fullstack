package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveReportDTO;
import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveReportService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveReportService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public LeaveReportDTO generateMonthlyReport() {
        // Mock generation: Just returning all leaves structured as a report for now
        List<LeaveResponseDTO> allLeaves = leaveRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new LeaveReportDTO("MONTHLY_LEAVE_REPORT", LocalDateTime.now().toString(), allLeaves);
    }

    private LeaveResponseDTO mapToResponse(LeaveRequest l) {
        return new LeaveResponseDTO(
                l.getId(),
                l.getEmployee().getId(),
                l.getEmployee().getFirstName() + " " + l.getEmployee().getLastName(),
                l.getLeaveType().name(),
                l.getFromDate(),
                l.getToDate(),
                l.getRemarks(),
                l.getStatus().name(),
                l.getAdminComments()
        );
    }
}
