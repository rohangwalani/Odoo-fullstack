package com.hackathon.backend.controller;

import com.hackathon.backend.dto.AdminDashboardResponse;
import com.hackathon.backend.dto.EmployeeDashboardResponse;
import com.hackathon.backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/employee")
    public ResponseEntity<EmployeeDashboardResponse> getEmployeeDashboard(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getEmployeeDashboard(authentication.getName()));
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }
}
