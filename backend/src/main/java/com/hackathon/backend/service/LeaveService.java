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

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    public LeaveResponseDTO applyForLeave(String email, LeaveRequestDTO dto) {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Employee not found"));
        
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(LeaveType.valueOf(dto.getLeaveType().toUpperCase()));
        request.setFromDate(dto.getFromDate());
        request.setToDate(dto.getToDate());
        request.setRemarks(dto.getRemarks());
        
        LeaveRequest saved = leaveRequestRepository.save(request);
        return mapToResponse(saved);
    }

    public List<LeaveResponseDTO> getMyLeaves(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Employee not found"));
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
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus(LeaveStatus.APPROVED);
        request.setAdminComments(comments);
        return mapToResponse(leaveRequestRepository.save(request));
    }

    public LeaveResponseDTO rejectLeave(Long id, String comments) {
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Leave request not found"));
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
