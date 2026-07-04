package com.hackathon.backend.employee;

import com.hackathon.backend.employee.dto.EmployeeRequest;
import com.hackathon.backend.employee.dto.EmployeeResponse;
import com.hackathon.backend.employee.dto.ProfileUpdateRequest;
import com.hackathon.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> addEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.addEmployee(userDetails.getCompanyId(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(employeeService.getEmployees(userDetails.getCompanyId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        // Employees can only view their own profile, Admins can view any in their company
        if (!userDetails.getAuthorities().iterator().next().getAuthority().equals("ROLE_ADMIN") && !userDetails.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(employeeService.getEmployeeById(id, userDetails.getCompanyId()));
    }

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> updateEmployeeByAdmin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployeeByAdmin(id, userDetails.getCompanyId(), request));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<EmployeeResponse> updateProfileByEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @ModelAttribute ProfileUpdateRequest request) {
        if (!userDetails.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(employeeService.updateProfileByEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        employeeService.deleteEmployee(id, userDetails.getCompanyId());
        return ResponseEntity.ok(Map.of("success", true, "message", "Employee deleted successfully"));
    }
}
