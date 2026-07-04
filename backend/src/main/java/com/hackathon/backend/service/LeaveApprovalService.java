package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveApprovalDTO;
import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.exception.InvalidLeaveStatusException;
import com.hackathon.backend.exception.LeaveNotFoundException;
import com.hackathon.backend.model.Attendance;
import com.hackathon.backend.model.AttendanceStatus;
import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.repository.AttendanceRepository;
import com.hackathon.backend.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class LeaveApprovalService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;
    private final AttendanceRepository attendanceRepository;

    public LeaveApprovalService(LeaveRequestRepository leaveRequestRepository,
                                NotificationService notificationService,
                                AttendanceRepository attendanceRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationService = notificationService;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public LeaveResponseDTO approveLeave(Long id, LeaveApprovalDTO approvalDTO) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException("Leave request not found"));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusException("Only pending leaves can be approved.");
        }

        request.setStatus(LeaveStatus.APPROVED);
        if (approvalDTO != null) {
            request.setAdminComments(approvalDTO.getComments());
        }

        // Update attendance for the approved leave days
        LocalDate currentDate = request.getFromDate();
        while (!currentDate.isAfter(request.getToDate())) {
            // Check if attendance record exists, if not create one
            LocalDate date = currentDate;
            Attendance attendance = attendanceRepository.findByEmployeeAndDate(request.getEmployee(), date)
                    .orElse(new Attendance());
            attendance.setEmployee(request.getEmployee());
            attendance.setDate(date);
            attendance.setStatus(AttendanceStatus.LEAVE);
            attendance.setRemarks(request.getLeaveType().name());
            attendanceRepository.save(attendance);
            
            currentDate = currentDate.plusDays(1);
        }

        LeaveRequest saved = leaveRequestRepository.save(request);

        notificationService.sendNotification(request.getEmployee(),
                "Your leave request for " + request.getFromDate() + " to " + request.getToDate() + " has been approved.");

        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponseDTO rejectLeave(Long id, LeaveApprovalDTO approvalDTO) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException("Leave request not found"));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusException("Only pending leaves can be rejected.");
        }

        request.setStatus(LeaveStatus.REJECTED);
        if (approvalDTO != null) {
            request.setAdminComments(approvalDTO.getComments());
        }

        LeaveRequest saved = leaveRequestRepository.save(request);

        notificationService.sendNotification(request.getEmployee(),
                "Your leave request for " + request.getFromDate() + " to " + request.getToDate() + " has been rejected.");

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
