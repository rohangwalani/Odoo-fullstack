package com.hackathon.backend.profile.dto;

import lombok.Data;
import java.util.List;
import java.time.LocalDate;

@Data
public class ProfileResponse {
    // Basic Info
    private Long employeeId;
    private String employeeCode; // loginId
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String avatar; // Maps to profilePicture
    private LocalDate joiningDate;

    // Resume / Profile Info
    private String about;
    private String jobDescription;
    private String hobbies;
    
    private List<SkillDTO> skills;
    private List<CertificationDTO> certifications;
}
