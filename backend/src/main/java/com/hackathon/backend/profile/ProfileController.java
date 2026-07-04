package com.hackathon.backend.profile;

import com.hackathon.backend.auth.AuthService;
import com.hackathon.backend.auth.dto.AuthResponse;
import com.hackathon.backend.auth.dto.ChangePasswordRequest;
import com.hackathon.backend.profile.dto.*;
import com.hackathon.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;

    public ProfileController(ProfileService profileService, AuthService authService) {
        this.profileService = profileService;
        this.authService = authService;
    }

    // ==============================================
    // RESUME / BASIC PROFILE
    // ==============================================

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profileService.getProfile(userDetails.getId()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userDetails.getId(), request));
    }

    @PutMapping("/avatar")
    public ResponseEntity<ProfileResponse> updateAvatar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("avatar") MultipartFile avatar) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setAvatar(avatar);
        return ResponseEntity.ok(profileService.updateProfile(userDetails.getId(), request));
    }

    // ==============================================
    // SKILLS
    // ==============================================

    @PostMapping("/skills")
    public ResponseEntity<ProfileResponse> addSkill(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SkillDTO request) {
        return ResponseEntity.ok(profileService.addSkill(userDetails.getId(), request));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<ProfileResponse> updateSkill(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SkillDTO request) {
        return ResponseEntity.ok(profileService.updateSkill(userDetails.getId(), id, request));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<ProfileResponse> deleteSkill(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(profileService.deleteSkill(userDetails.getId(), id));
    }

    // ==============================================
    // CERTIFICATIONS
    // ==============================================

    @PostMapping("/certifications")
    public ResponseEntity<ProfileResponse> addCertification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CertificationDTO request) {
        return ResponseEntity.ok(profileService.addCertification(userDetails.getId(), request));
    }

    @PutMapping("/certifications/{id}")
    public ResponseEntity<ProfileResponse> updateCertification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CertificationDTO request) {
        return ResponseEntity.ok(profileService.updateCertification(userDetails.getId(), id, request));
    }

    @DeleteMapping("/certifications/{id}")
    public ResponseEntity<ProfileResponse> deleteCertification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(profileService.deleteCertification(userDetails.getId(), id));
    }

    // ==============================================
    // PRIVATE INFORMATION
    // ==============================================

    @GetMapping("/private")
    public ResponseEntity<PrivateInfoResponse> getPrivateInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profileService.getPrivateInfo(userDetails.getId()));
    }

    @PutMapping("/private")
    public ResponseEntity<PrivateInfoResponse> updatePrivateInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PrivateInfoUpdateRequest request) {
        return ResponseEntity.ok(profileService.updatePrivateInfo(userDetails.getId(), request));
    }

    // ==============================================
    // SECURITY
    // ==============================================

    @PutMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(userDetails.getId(), request));
    }
}
