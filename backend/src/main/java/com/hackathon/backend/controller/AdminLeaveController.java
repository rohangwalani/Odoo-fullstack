package com.hackathon.backend.controller;

import com.hackathon.backend.dto.LeaveApprovalDTO;
import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.dto.LeaveSearchDTO;
import com.hackathon.backend.dto.LeaveStatisticsDTO;
import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.repository.LeaveRequestRepository;
import com.hackathon.backend.service.LeaveApprovalService;
import com.hackathon.backend.service.LeaveService;
import com.hackathon.backend.service.LeaveStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/leaves")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class AdminLeaveController {

    private final LeaveService leaveService;
    private final LeaveApprovalService leaveApprovalService;
    private final LeaveStatisticsService leaveStatisticsService;
    private final LeaveRequestRepository leaveRequestRepository;

    public AdminLeaveController(LeaveService leaveService,
                                LeaveApprovalService leaveApprovalService,
                                LeaveStatisticsService leaveStatisticsService,
                                LeaveRequestRepository leaveRequestRepository) {
        this.leaveService = leaveService;
        this.leaveApprovalService = leaveApprovalService;
        this.leaveStatisticsService = leaveStatisticsService;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @GetMapping
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveResponseDTO> approveLeave(@PathVariable Long id, @RequestBody(required = false) LeaveApprovalDTO dto) {
        return ResponseEntity.ok(leaveApprovalService.approveLeave(id, dto));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveResponseDTO> rejectLeave(@PathVariable Long id, @RequestBody(required = false) LeaveApprovalDTO dto) {
        return ResponseEntity.ok(leaveApprovalService.rejectLeave(id, dto));
    }

    @GetMapping("/statistics")
    public ResponseEntity<LeaveStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(leaveStatisticsService.getStatistics());
    }

    @PostMapping("/search")
    public ResponseEntity<List<LeaveResponseDTO>> searchLeaves(@RequestBody LeaveSearchDTO searchDTO) {
        // Simple in-memory search for brevity; ideally this should be a JPA Specification
        List<LeaveRequest> allLeaves = leaveRequestRepository.findAll();

        List<LeaveResponseDTO> filtered = allLeaves.stream()
                .filter(l -> searchDTO.getEmployeeName() == null || (l.getEmployee().getFirstName() + " " + l.getEmployee().getLastName()).toLowerCase().contains(searchDTO.getEmployeeName().toLowerCase()))
                .filter(l -> searchDTO.getLeaveType() == null || l.getLeaveType().name().equalsIgnoreCase(searchDTO.getLeaveType()))
                .filter(l -> searchDTO.getStatus() == null || l.getStatus().name().equalsIgnoreCase(searchDTO.getStatus()))
                .filter(l -> searchDTO.getFromDate() == null || !l.getFromDate().isBefore(searchDTO.getFromDate()))
                .filter(l -> searchDTO.getToDate() == null || !l.getToDate().isAfter(searchDTO.getToDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
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
