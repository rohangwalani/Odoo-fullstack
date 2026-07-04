package com.hackathon.backend.profile;

import com.hackathon.backend.employee.EmployeeService;
import com.hackathon.backend.employee.dto.EmployeeResponse;
import com.hackathon.backend.employee.dto.ProfileUpdateRequest;
import com.hackathon.backend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final EmployeeService employeeService;

    public ProfileController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<EmployeeResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(employeeService.getEmployeeById(userDetails.getId(), userDetails.getCompanyId()));
    }

    @PutMapping
    public ResponseEntity<EmployeeResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        // Exclude avatar from this request since it comes from avatar upload
        return ResponseEntity.ok(employeeService.updateProfileByEmployee(userDetails.getId(), request));
    }

    @PutMapping("/avatar")
    public ResponseEntity<EmployeeResponse> updateAvatar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("avatar") MultipartFile avatar) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setProfilePicture(avatar);
        return ResponseEntity.ok(employeeService.updateProfileByEmployee(userDetails.getId(), request));
    }
}
