package com.hackathon.backend.service;

import com.hackathon.backend.dto.LeaveRequestDTO;
import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.model.LeaveStatus;
import com.hackathon.backend.model.LeaveType;
import com.hackathon.backend.model.Employee;
import com.hackathon.backend.repository.LeaveRequestRepository;
import com.hackathon.backend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hackathon.backend.exception.InvalidLeaveException;
import com.hackathon.backend.exception.LeaveNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.time.LocalDate;

@Service
public class LeaveService {

    private static final Logger log = LoggerFactory.getLogger(LeaveService.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository, NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    public LeaveResponseDTO applyForLeave(String email, LeaveRequestDTO dto) {
        log.info("Applying for leave for user: {}", email);
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(LeaveType.valueOf(dto.getLeaveType().toUpperCase()));
        request.setFromDate(dto.getFromDate());
        request.setToDate(dto.getToDate());
        request.setRemarks(dto.getRemarks());

        // Validate Past Dates
        if (request.getFromDate().isBefore(LocalDate.now())) {
            log.warn("Cannot apply for leave in the past: {}", request.getFromDate());
            throw new InvalidLeaveException("Cannot apply for leave on past dates.");
        }
        if (request.getToDate().isBefore(request.getFromDate())) {
            log.warn("To Date {} is before From Date {}", request.getToDate(), request.getFromDate());
            throw new InvalidLeaveException("End date cannot be before start date.");
        }

        // Validate Overlap with existing approved leaves
        boolean hasOverlap = leaveRequestRepository.findByEmployee(employee).stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .anyMatch(l -> !(request.getToDate().isBefore(l.getFromDate()) || request.getFromDate().isAfter(l.getToDate())));

        if (hasOverlap) {
            log.warn("Leave overlaps with existing approved leave for user {}", email);
            throw new InvalidLeaveException("Cannot apply for leave that overlaps with an existing approved leave.");
        }
        
        LeaveRequest saved = leaveRequestRepository.save(request);
        notificationService.sendLeaveAppliedNotification(employee, saved);
        return mapToResponse(saved);
    }

    public List<LeaveResponseDTO> getMyLeaves(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return leaveRequestRepository.findByEmployee(employee).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LeaveResponseDTO approveLeave(Long id, String comments) {
        log.info("Approving leave request {}", id);
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow(() -> new LeaveNotFoundException("Leave request not found"));
        request.setStatus(LeaveStatus.APPROVED);
        request.setAdminComments(comments);
        return mapToResponse(leaveRequestRepository.save(request));
    }

    public LeaveResponseDTO rejectLeave(Long id, String comments) {
        log.info("Rejecting leave request {}", id);
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow(() -> new LeaveNotFoundException("Leave request not found"));
        request.setStatus(LeaveStatus.REJECTED);
        request.setAdminComments(comments);
        return mapToResponse(leaveRequestRepository.save(request));
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
