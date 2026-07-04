package com.hackathon.backend.controller;

import com.hackathon.backend.dto.*;
import com.hackathon.backend.service.LeaveApprovalService;
import com.hackathon.backend.service.LeaveReportService;
import com.hackathon.backend.service.LeaveService;
import com.hackathon.backend.service.LeaveStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/leaves")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLeaveController {

    private final LeaveService leaveService;
    private final LeaveApprovalService leaveApprovalService;
    private final LeaveStatisticsService leaveStatisticsService;
    private final LeaveReportService leaveReportService;

    public AdminLeaveController(LeaveService leaveService, 
                                LeaveApprovalService leaveApprovalService, 
                                LeaveStatisticsService leaveStatisticsService, 
                                LeaveReportService leaveReportService) {
        this.leaveService = leaveService;
        this.leaveApprovalService = leaveApprovalService;
        this.leaveStatisticsService = leaveStatisticsService;
        this.leaveReportService = leaveReportService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveResponseDTO> approveLeave(@PathVariable Long id, @RequestBody(required = false) LeaveApprovalDTO dto) {
        String comments = dto != null ? dto.getAdminComments() : null;
        return ResponseEntity.ok(leaveApprovalService.approveLeave(id, comments));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveResponseDTO> rejectLeave(@PathVariable Long id, @RequestBody(required = false) LeaveApprovalDTO dto) {
        String comments = dto != null ? dto.getAdminComments() : null;
        return ResponseEntity.ok(leaveApprovalService.rejectLeave(id, comments));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LeaveResponseDTO>> searchLeaves(
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        // For simplicity in Phase 5, we are just returning all leaves. 
        // In a real scenario, we'd pass these params to a JpaSpecificationExecutor in LeaveRequestRepository
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/statistics")
    public ResponseEntity<LeaveStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(leaveStatisticsService.getStatistics());
    }

    @GetMapping("/report")
    public ResponseEntity<LeaveReportDTO> generateReport() {
        return ResponseEntity.ok(leaveReportService.generateMonthlyReport());
    }
}
