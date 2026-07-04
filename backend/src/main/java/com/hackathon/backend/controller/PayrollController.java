package com.hackathon.backend.controller;

import com.hackathon.backend.dto.PayrollRequestDTO;
import com.hackathon.backend.dto.PayrollResponse;
import com.hackathon.backend.service.PayrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyPayroll(Authentication authentication) {
        try {
            return ResponseEntity.ok(payrollService.getMyPayroll(authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PayrollResponse>> getAllPayrolls() {
        return ResponseEntity.ok(payrollService.getAllPayrolls());
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PayrollResponse> updatePayroll(@PathVariable Long employeeId, @RequestBody PayrollRequestDTO dto) {
        return ResponseEntity.ok(payrollService.updatePayroll(employeeId, dto));
    }
}
