package com.hackathon.backend.controller;

import com.hackathon.backend.dto.SalaryResponse;
import com.hackathon.backend.dto.SalaryUpdateRequest;
import com.hackathon.backend.service.SalaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salary")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<SalaryResponse> getSalary(@PathVariable Long employeeId) {
        return ResponseEntity.ok(salaryService.getSalary(employeeId));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalaryResponse> updateSalary(
            @PathVariable Long employeeId,
            @RequestBody SalaryUpdateRequest request) {
        return ResponseEntity.ok(salaryService.updateSalary(employeeId, request));
    }
}
