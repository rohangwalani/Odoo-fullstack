package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.exception.InvalidLeaveStatusException;
import com.hackathon.backend.exception.LeaveNotFoundException;
import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.repository.LeaveRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class LeaveApprovalService {

    private static final Logger log = LoggerFactory.getLogger(LeaveApprovalService.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;

    public LeaveApprovalService(LeaveRequestRepository leaveRequestRepository, NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationService = notificationService;
    }

    public LeaveResponseDTO approveLeave(Long id, String comments) {
        log.info("Admin approving leave request {}", id);
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException("Leave request not found"));

        if (request.getStatus() == LeaveStatus.APPROVED) {
            throw new InvalidLeaveStatusException("Leave is already approved.");
        }

        request.setStatus(LeaveStatus.APPROVED);
        request.setAdminComments(comments);
        
        LeaveRequest saved = leaveRequestRepository.save(request);
        
        // Notify employee
        notificationService.sendLeaveApprovedNotification(request.getEmployee(), saved);

        return mapToResponse(saved);
    }

    public LeaveResponseDTO rejectLeave(Long id, String comments) {
        log.info("Admin rejecting leave request {}", id);
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException("Leave request not found"));

        if (request.getStatus() == LeaveStatus.REJECTED) {
            throw new InvalidLeaveStatusException("Leave is already rejected.");
        }

        request.setStatus(LeaveStatus.REJECTED);
        request.setAdminComments(comments);

        LeaveRequest saved = leaveRequestRepository.save(request);
        
        // Notify employee
        notificationService.sendLeaveRejectedNotification(request.getEmployee(), saved);

        return mapToResponse(saved);
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
